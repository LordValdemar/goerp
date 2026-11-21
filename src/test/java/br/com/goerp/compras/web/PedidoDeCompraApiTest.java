package br.com.goerp.compras.web;

import static br.com.goerp.usuario.dominio.Permissao.COMPRA_LER;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
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
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import com.jayway.jsonpath.JsonPath;

import br.com.goerp.empresa.dominio.Empresa;
import br.com.goerp.suporte.Cenario;
import br.com.goerp.suporte.Login;
import br.com.goerp.suporte.TesteDeIntegracao;

class PedidoDeCompraApiTest extends TesteDeIntegracao {

    @Autowired
    MockMvc mvc;

    @Autowired
    Cenario cenario;

    Empresa empresaA;
    String admin;
    String adminDaB;
    Integer arroz;
    Integer feijao;
    Integer servico;
    Integer fornecedor;
    Integer fornecedorDaB;

    @BeforeEach
    void preparar() throws Exception {
        empresaA = cenario.empresa("11222333000181", "Mercado A LTDA");
        Empresa empresaB = cenario.empresa("12345678000195", "Mercado B LTDA");
        cenario.vincular(cenario.usuario("admin@a.com"), empresaA);
        cenario.vincular(cenario.usuario("admin@b.com"), empresaB);
        admin = Login.bearer(mvc, "admin@a.com", empresaA.id());
        adminDaB = Login.bearer(mvc, "admin@b.com", empresaB.id());
        arroz = produto("ARZ", "18.5000", true);
        feijao = produto("FEJ", "7.2500", true);
        servico = produto("FRT", "0", false);
        fornecedor = idDe(enviar(post("/api/v1/fornecedores"), admin,
                "{\"documento\":\"11444777000161\",\"nome\":\"Atacado Bom Preço LTDA\"}"));
        fornecedorDaB = idDe(enviar(post("/api/v1/fornecedores"), adminDaB,
                "{\"documento\":\"11444777000161\",\"nome\":\"Atacado da B\"}"));
    }

    @Test
    void criaRascunhoComCustoDoCadastroOuNegociado() throws Exception {
        criar(admin, fornecedor, """
                [{"produtoId":%d,"quantidade":100},{"produtoId":%d,"quantidade":50,"custoNegociado":7.1}]"""
                .formatted(arroz, feijao), hoje().plusDays(5))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.numero").value(1))
                .andExpect(jsonPath("$.situacao").value("RASCUNHO"))
                .andExpect(jsonPath("$.itens[0].custoNegociado").value(18.5))
                .andExpect(jsonPath("$.itens[1].custoNegociado").value(7.1))
                .andExpect(jsonPath("$.itens[1].pendente").value(50))
                .andExpect(jsonPath("$.total").value(2205.0));
    }

    @Test
    void alteraRascunhoComVersaoEDepoisDeEnviadoNao() throws Exception {
        Integer pedido = idDe(criarSimples());

        alterar(pedido, 0, 200).andExpect(status().isOk()).andExpect(jsonPath("$.total").value(3700.0));
        alterar(pedido, 0, 300).andExpect(status().isConflict());

        acao(pedido, "enviar").andExpect(jsonPath("$.situacao").value("ENVIADO"));
        alterar(pedido, 2, 300).andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.detail").value("Pedido de compra enviado não pode ser alterado"));
    }

    @Test
    void regrasDosItensEDoFornecedor() throws Exception {
        criar(admin, fornecedor, "[{\"produtoId\":%d,\"quantidade\":1}]".formatted(servico), null)
                .andExpect(status().isUnprocessableEntity());
        criar(admin, fornecedor, "[{\"produtoId\":%d,\"quantidade\":1.5}]".formatted(arroz), null)
                .andExpect(status().isUnprocessableEntity());
        criar(admin, fornecedor, "[{\"produtoId\":%d,\"quantidade\":1},{\"produtoId\":%d,\"quantidade\":2}]"
                .formatted(arroz, arroz), null).andExpect(status().isUnprocessableEntity());
        criar(admin, fornecedorDaB, "[{\"produtoId\":%d,\"quantidade\":1}]".formatted(arroz), null)
                .andExpect(status().isNotFound());
        criar(adminDaB, fornecedorDaB, "[{\"produtoId\":%d,\"quantidade\":1}]".formatted(arroz), null)
                .andExpect(status().isNotFound());

        mvc.perform(delete("/api/v1/fornecedores/" + fornecedor).header("Authorization", admin));
        criarSimples().andExpect(status().isUnprocessableEntity());
    }

    @Test
    void cancelaSemRecebimentoEEncerrarSaldoExigeRecebimentoParcial() throws Exception {
        Integer pedido = idDe(criarSimples());
        acao(pedido, "enviar");

        enviar(post("/api/v1/pedidos-compra/%d/encerrar-saldo".formatted(pedido)), admin, "{\"motivo\":\"Sem estoque\"}")
                .andExpect(status().isUnprocessableEntity());
        enviar(post("/api/v1/pedidos-compra/%d/cancelar".formatted(pedido)), admin, "{\"motivo\":\"Achei mais barato\"}")
                .andExpect(jsonPath("$.situacao").value("CANCELADO"))
                .andExpect(jsonPath("$.motivoDoEncerramento").value("Achei mais barato"));
    }

    @Test
    void ficaAtrasadoQuandoAPrevisaoPassa() throws Exception {
        Integer pedido = idDe(criar(admin, fornecedor, "[{\"produtoId\":%d,\"quantidade\":10}]".formatted(arroz),
                hoje().plusDays(2)));
        acao(pedido, "enviar");

        buscar(pedido).andExpect(jsonPath("$.atrasado").value(false));
        relogio.avancar(Duration.ofDays(3));
        buscar(pedido).andExpect(jsonPath("$.atrasado").value(true));
    }

    @Test
    void pesquisaIsolamentoEHistorico() throws Exception {
        Integer primeiro = idDe(criarSimples());
        Integer segundo = idDe(criarSimples());
        acao(segundo, "enviar");

        mvc.perform(get("/api/v1/pedidos-compra?fornecedorId=" + fornecedor).header("Authorization", admin))
                .andExpect(jsonPath("$.itens[*].numero", contains(2, 1)));
        mvc.perform(get("/api/v1/pedidos-compra?situacao=ENVIADO").header("Authorization", admin))
                .andExpect(jsonPath("$.itens[*].numero", contains(2)));
        mvc.perform(get("/api/v1/pedidos-compra/" + primeiro).header("Authorization", adminDaB))
                .andExpect(status().isNotFound());
        mvc.perform(post("/api/v1/pedidos-compra/%d/enviar".formatted(primeiro)).header("Authorization", adminDaB))
                .andExpect(status().isNotFound());

        assertThat(jdbc.queryForList("SELECT situacao FROM pedido_compra_aud WHERE id = ? ORDER BY rev", String.class,
                segundo)).containsExactly("RASCUNHO", "ENVIADO");
    }

    @Test
    void soQuemGerenciaComprasCriaPedido() throws Exception {
        cenario.vincular(cenario.usuario("leitor@a.com"), empresaA, cenario.perfil(empresaA, "Leitor", COMPRA_LER));
        String leitor = Login.bearer(mvc, "leitor@a.com", empresaA.id());

        criar(leitor, fornecedor, "[{\"produtoId\":%d,\"quantidade\":1}]".formatted(arroz), null)
                .andExpect(status().isForbidden());
        mvc.perform(get("/api/v1/pedidos-compra").header("Authorization", leitor)).andExpect(status().isOk());
    }

    private ResultActions criarSimples() throws Exception {
        return criar(admin, fornecedor, "[{\"produtoId\":%d,\"quantidade\":100}]".formatted(arroz), null);
    }

    private ResultActions criar(String token, Integer fornecedorId, String itens, LocalDate previsao) throws Exception {
        return enviar(post("/api/v1/pedidos-compra"), token, "{\"fornecedorId\":%d,\"itens\":%s,\"previsaoDeEntrega\":%s}"
                .formatted(fornecedorId, itens, previsao == null ? "null" : "\"" + previsao + "\""));
    }

    private ResultActions alterar(Integer pedido, int versao, int quantidade) throws Exception {
        return enviar(put("/api/v1/pedidos-compra/" + pedido), admin, "{\"versao\":%d,\"itens\":[{\"produtoId\":%d,\"quantidade\":%d}]}"
                .formatted(versao, arroz, quantidade));
    }

    private ResultActions acao(Integer pedido, String acao) throws Exception {
        return mvc.perform(post("/api/v1/pedidos-compra/%d/%s".formatted(pedido, acao)).header("Authorization", admin));
    }

    private ResultActions buscar(Integer pedido) throws Exception {
        return mvc.perform(get("/api/v1/pedidos-compra/" + pedido).header("Authorization", admin));
    }

    private ResultActions enviar(MockHttpServletRequestBuilder requisicao, String token, String corpo) throws Exception {
        return mvc.perform(requisicao.header("Authorization", token).contentType(MediaType.APPLICATION_JSON).content(corpo));
    }

    private Integer produto(String codigo, String custo, boolean controlaEstoque) throws Exception {
        Long un = jdbc.queryForObject("SELECT id FROM unidade_medida WHERE empresa_id = ? AND sigla = 'UN'", Long.class,
                empresaA.id());
        return idDe(enviar(post("/api/v1/produtos"), admin, """
                {"codigo":"%s","descricao":"Produto %s","unidadeId":%d,"precoDeVenda":30,"precoDeCusto":%s,
                 "ncm":"10063021","controlaEstoque":%s}""".formatted(codigo, codigo, un, custo, controlaEstoque)));
    }

    private LocalDate hoje() {
        return LocalDate.ofInstant(relogio.instant(), ZoneId.of("America/Sao_Paulo"));
    }

    private static Integer idDe(ResultActions resposta) throws Exception {
        return JsonPath.read(resposta.andReturn().getResponse().getContentAsString(), "$.id");
    }
}
