package br.com.goerp.vendas.web;

import static br.com.goerp.usuario.dominio.Permissao.PEDIDO_GERENCIAR;
import static br.com.goerp.usuario.dominio.Permissao.PEDIDO_LER;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Duration;
import java.time.LocalDate;
import java.time.ZoneId;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import com.jayway.jsonpath.JsonPath;

import br.com.goerp.empresa.dominio.Empresa;
import br.com.goerp.suporte.Cenario;
import br.com.goerp.suporte.Login;
import br.com.goerp.suporte.TesteDeIntegracao;

class PedidoApiTest extends TesteDeIntegracao {

    @Autowired
    MockMvc mvc;

    @Autowired
    Cenario cenario;

    Empresa empresaA;
    String admin;
    String vendedor;
    String adminDaB;
    Integer arroz;
    Integer cliente;

    @BeforeEach
    void preparar() throws Exception {
        empresaA = cenario.empresa("11222333000181", "Distribuidora A LTDA");
        Empresa empresaB = cenario.empresa("12345678000195", "Distribuidora B LTDA");
        cenario.vincular(cenario.usuario("admin@a.com"), empresaA);
        cenario.vincular(cenario.usuario("admin@b.com"), empresaB);
        cenario.vincular(cenario.usuario("vendedor@a.com"), empresaA,
                cenario.perfil(empresaA, "Vendedor", PEDIDO_LER, PEDIDO_GERENCIAR));
        admin = Login.bearer(mvc, "admin@a.com", empresaA.id());
        adminDaB = Login.bearer(mvc, "admin@b.com", empresaB.id());
        vendedor = Login.bearer(mvc, "vendedor@a.com", empresaA.id());

        Long un = jdbc.queryForObject("SELECT id FROM unidade_medida WHERE empresa_id = ? AND sigla = 'UN'", Long.class,
                empresaA.id());
        arroz = idDe(enviar(post("/api/v1/produtos"), admin, """
                {"codigo":"ARZ","descricao":"Arroz 5kg","unidadeId":%d,"precoDeVenda":24.90,"ncm":"10063021",
                 "controlaEstoque":true}""".formatted(un)));
        cliente = idDe(enviar(post("/api/v1/clientes"), admin,
                "{\"documento\":\"11444777000161\",\"nome\":\"Restaurante Sabor LTDA\"}"));
        enviar(post("/api/v1/estoque/entradas"), admin,
                "{\"produtoId\":%d,\"quantidade\":30,\"custoUnitario\":18}".formatted(arroz)).andExpect(status().isCreated());
    }

    @Test
    void orcamentoConfirmadoEFaturadoBaixaOEstoque() throws Exception {
        Integer pedido = idDe(criar(vendedor, 10).andExpect(status().isCreated())
                .andExpect(jsonPath("$.numero").value(1))
                .andExpect(jsonPath("$.situacao").value("ORCAMENTO"))
                .andExpect(jsonPath("$.total").value(249.0)));
        saldoDoArroz().andExpect(jsonPath("$.quantidade").value(30));

        acao(vendedor, pedido, "confirmar").andExpect(jsonPath("$.situacao").value("CONFIRMADO"));
        acao(vendedor, pedido, "faturar").andExpect(status().isForbidden());
        acao(admin, pedido, "faturar").andExpect(status().isOk()).andExpect(jsonPath("$.situacao").value("FATURADO"));

        saldoDoArroz().andExpect(jsonPath("$.quantidade").value(20));
        mvc.perform(get("/api/v1/estoque/produtos/" + arroz + "/extrato").header("Authorization", admin))
                .andExpect(jsonPath("$.itens[0].origem").value("PEDIDO_DE_VENDA"))
                .andExpect(jsonPath("$.itens[0].documentoDeOrigem").value(pedido));
    }

    @Test
    void faturamentoSemEstoqueNaoMudaNada() throws Exception {
        Integer pedido = idDe(criar(vendedor, 31));
        acao(vendedor, pedido, "confirmar");

        acao(admin, pedido, "faturar").andExpect(status().isUnprocessableEntity());

        mvc.perform(get("/api/v1/pedidos-venda/" + pedido).header("Authorization", admin))
                .andExpect(jsonPath("$.situacao").value("CONFIRMADO"));
        saldoDoArroz().andExpect(jsonPath("$.quantidade").value(30));
    }

    @Test
    void alteraOrcamentoRecopiandoOPrecoEDetectaEdicaoSimultanea() throws Exception {
        Integer pedido = idDe(criar(vendedor, 10));
        jdbc.update("UPDATE produto SET preco_venda = 25.90 WHERE id = ?", arroz);

        alterar(vendedor, pedido, 0, 4).andExpect(status().isOk())
                .andExpect(jsonPath("$.itens[0].precoUnitario").value(25.90))
                .andExpect(jsonPath("$.total").value(103.6));
        alterar(vendedor, pedido, 0, 5).andExpect(status().isConflict());

        var historico = jdbc.queryForList("SELECT total FROM pedido_venda_aud WHERE id = ? ORDER BY rev", pedido);
        assertThat(historico).extracting(linha -> linha.get("total").toString()).containsExactly("249.00", "103.60");
    }

    @Test
    void confirmadoNaoAlteraEOrcamentoVencidoNaoConfirma() throws Exception {
        Integer confirmado = idDe(criar(vendedor, 1));
        acao(vendedor, confirmado, "confirmar");
        alterar(vendedor, confirmado, 1, 2).andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.detail").value("Pedido confirmado não pode ser alterado"));

        Integer vencido = idDe(criar(vendedor, 1));
        relogio.avancar(Duration.ofDays(8));
        acao(vendedor, vencido, "confirmar").andExpect(status().isUnprocessableEntity());
    }

    @Test
    void cancelaAntesDeFaturarMasNaoDepois() throws Exception {
        Integer pedido = idDe(criar(vendedor, 1));
        cancelar(vendedor, pedido).andExpect(jsonPath("$.situacao").value("CANCELADO"));

        Integer faturado = idDe(criar(vendedor, 1));
        acao(vendedor, faturado, "confirmar");
        acao(admin, faturado, "faturar");
        cancelar(vendedor, faturado).andExpect(status().isUnprocessableEntity());
    }

    @Test
    void exigeClienteDaEmpresaEValidadeFutura() throws Exception {
        enviar(post("/api/v1/pedidos-venda"), vendedor, corpo(null, 1, hoje().plusDays(7)))
                .andExpect(status().isUnprocessableEntity());
        enviar(post("/api/v1/pedidos-venda"), vendedor, corpo(cliente, 1, hoje().minusDays(1)))
                .andExpect(status().isUnprocessableEntity());
        enviar(post("/api/v1/pedidos-venda"), adminDaB, corpo(cliente, 1, hoje().plusDays(7)))
                .andExpect(status().isNotFound());
    }

    @Test
    void pesquisaENumeracaoPorEmpresa() throws Exception {
        criar(vendedor, 1);
        Integer segundo = idDe(criar(vendedor, 2));
        acao(vendedor, segundo, "confirmar");

        mvc.perform(get("/api/v1/pedidos-venda?clienteId=" + cliente).header("Authorization", vendedor))
                .andExpect(jsonPath("$.itens[*].numero", contains(2, 1)));
        mvc.perform(get("/api/v1/pedidos-venda?situacao=CONFIRMADO").header("Authorization", vendedor))
                .andExpect(jsonPath("$.itens[*].numero", contains(2)));
        mvc.perform(get("/api/v1/pedidos-venda").header("Authorization", adminDaB)).andExpect(jsonPath("$.total").value(0));
        mvc.perform(get("/api/v1/pedidos-venda/" + segundo).header("Authorization", adminDaB))
                .andExpect(status().isNotFound());
    }

    private ResultActions criar(String token, int quantidade) throws Exception {
        return enviar(post("/api/v1/pedidos-venda"), token, corpo(cliente, quantidade, hoje().plusDays(7)));
    }

    private ResultActions alterar(String token, Integer pedido, int versao, int quantidade) throws Exception {
        return enviar(put("/api/v1/pedidos-venda/" + pedido), token, """
                {"versao":%d,"itens":[{"produtoId":%d,"quantidade":%d}],"validade":"%s"}"""
                .formatted(versao, arroz, quantidade, hoje().plusDays(7)));
    }

    private ResultActions acao(String token, Integer pedido, String acao) throws Exception {
        return mvc.perform(post("/api/v1/pedidos-venda/%d/%s".formatted(pedido, acao)).header("Authorization", token));
    }

    private ResultActions cancelar(String token, Integer pedido) throws Exception {
        return enviar(post("/api/v1/pedidos-venda/%d/cancelar".formatted(pedido)), token, "{\"motivo\":\"Desistiu\"}");
    }

    private String corpo(Integer clienteId, int quantidade, LocalDate validade) {
        return """
                {"clienteId":%s,"itens":[{"produtoId":%d,"quantidade":%d}],"validade":"%s"}"""
                .formatted(clienteId, arroz, quantidade, validade);
    }

    private ResultActions saldoDoArroz() throws Exception {
        return mvc.perform(get("/api/v1/estoque/saldos/" + arroz).header("Authorization", admin));
    }

    private ResultActions enviar(org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder requisicao,
            String token, String corpo) throws Exception {
        return mvc.perform(requisicao.header("Authorization", token).contentType(MediaType.APPLICATION_JSON).content(corpo));
    }

    private LocalDate hoje() {
        return LocalDate.ofInstant(relogio.instant(), ZoneId.of("America/Sao_Paulo"));
    }

    private static Integer idDe(ResultActions resposta) throws Exception {
        return JsonPath.read(resposta.andReturn().getResponse().getContentAsString(), "$.id");
    }
}
