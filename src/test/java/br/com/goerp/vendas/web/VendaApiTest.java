package br.com.goerp.vendas.web;

import static br.com.goerp.usuario.dominio.Permissao.CAIXA_OPERAR;
import static br.com.goerp.usuario.dominio.Permissao.VENDA_DESCONTO;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
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

class VendaApiTest extends TesteDeIntegracao {

    private static final String GTIN_REFRIGERANTE = "7891000315507";

    @Autowired
    MockMvc mvc;

    @Autowired
    Cenario cenario;

    Empresa empresaA;
    String admin;
    String operadora;
    String operadoraComDesconto;
    String adminDaB;
    Integer refrigerante;
    Integer queijo;
    Integer servico;

    @BeforeEach
    void preparar() throws Exception {
        empresaA = cenario.empresa("11222333000181", "Mercado A LTDA");
        Empresa empresaB = cenario.empresa("12345678000195", "Mercado B LTDA");
        cenario.vincular(cenario.usuario("admin@a.com"), empresaA);
        cenario.vincular(cenario.usuario("admin@b.com"), empresaB);
        admin = Login.bearer(mvc, "admin@a.com", empresaA.id());
        adminDaB = Login.bearer(mvc, "admin@b.com", empresaB.id());
        cenario.vincular(cenario.usuario("ana@a.com"), empresaA, cenario.perfil(empresaA, "Caixa", CAIXA_OPERAR));
        cenario.vincular(cenario.usuario("bia@a.com"), empresaA,
                cenario.perfil(empresaA, "Caixa sênior", CAIXA_OPERAR, VENDA_DESCONTO));
        operadora = Login.bearer(mvc, "ana@a.com", empresaA.id());
        operadoraComDesconto = Login.bearer(mvc, "bia@a.com", empresaA.id());

        refrigerante = produto("REF", "Refrigerante Cola 2L", "UN", "8.99", GTIN_REFRIGERANTE, true);
        queijo = produto("QJO", "Queijo Minas", "KG", "49.90", null, true);
        servico = produto("SRV", "Taxa de entrega", "UN", "5.00", null, false);
        entrada(refrigerante, "10");
        entrada(queijo, "5");
        abrirCaixa(operadora, "100");
        abrirCaixa(operadoraComDesconto, "100");
    }

    @Test
    void vendaCompletaComPrecoDoCadastroTrocoEBaixaDeEstoque() throws Exception {
        String venda = vender(operadora, """
                {"itens":[{"codigoDeBarras":"%s","quantidade":2},{"produtoId":%d,"quantidade":0.350},
                          {"produtoId":%d,"quantidade":1}],
                 "pagamentos":[{"forma":"PIX","valor":20},{"forma":"DINHEIRO","valor":30}],
                 "cpfNaNota":"529.982.247-25"}""".formatted(GTIN_REFRIGERANTE, queijo, servico))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.numero").value(1))
                .andExpect(jsonPath("$.itens[*].codigo", contains("REF", "QJO", "SRV")))
                .andExpect(jsonPath("$.itens[0].precoUnitario").value(8.99))
                .andExpect(jsonPath("$.itens[1].total").value(17.46))
                .andExpect(jsonPath("$.total").value(40.44))
                .andExpect(jsonPath("$.troco").value(9.56))
                .andExpect(jsonPath("$.cpfNaNota").value("529.982.247-25"))
                .andReturn().getResponse().getContentAsString();

        saldo(refrigerante).andExpect(jsonPath("$.quantidade").value(8));
        saldo(queijo).andExpect(jsonPath("$.quantidade").value(4.65));
        Integer vendaId = JsonPath.read(venda, "$.id");
        mvc.perform(get("/api/v1/estoque/produtos/" + refrigerante + "/extrato").header("Authorization", admin))
                .andExpect(jsonPath("$.itens[0].origem").value("VENDA"))
                .andExpect(jsonPath("$.itens[0].documentoDeOrigem").value(vendaId));
        assertThat(jdbc.queryForObject("SELECT count(*) FROM movimentacao_estoque WHERE produto_id = ?", Integer.class,
                servico)).isZero();
    }

    @Test
    void fechamentoDoCaixaEsperaOQueAsVendasTrouxeram() throws Exception {
        vender(operadora, umRefrigerante("[{\"forma\":\"DINHEIRO\",\"valor\":20}]"));
        vender(operadora, umRefrigerante("[{\"forma\":\"CARTAO_DEBITO\",\"valor\":8.99}]"));

        mvc.perform(get("/api/v1/caixas/meu").header("Authorization", operadora))
                .andExpect(jsonPath("$.esperado.DINHEIRO").value(108.99))
                .andExpect(jsonPath("$.esperado.CARTAO_DEBITO").value(8.99));
    }

    @Test
    void semEstoqueNadaEhGravadoNemONumero() throws Exception {
        vender(operadora, """
                {"itens":[{"produtoId":%d,"quantidade":1},{"produtoId":%d,"quantidade":11}],
                 "pagamentos":[{"forma":"DINHEIRO","valor":200}]}""".formatted(queijo, refrigerante))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.detail").value("Estoque insuficiente: disponível 10, pedido 11"));

        assertThat(jdbc.queryForObject("SELECT count(*) FROM venda", Integer.class)).isZero();
        saldo(queijo).andExpect(jsonPath("$.quantidade").value(5));
        vender(operadora, umRefrigerante("[{\"forma\":\"DINHEIRO\",\"valor\":10}]"))
                .andExpect(jsonPath("$.numero").value(1));
    }

    @Test
    void numeracaoSequencialPorEmpresa() throws Exception {
        vender(operadora, umRefrigerante("[{\"forma\":\"PIX\",\"valor\":8.99}]")).andExpect(jsonPath("$.numero").value(1));
        vender(operadoraComDesconto, umRefrigerante("[{\"forma\":\"PIX\",\"valor\":8.99}]"))
                .andExpect(jsonPath("$.numero").value(2));
        vender(operadora, umRefrigerante("[{\"forma\":\"PIX\",\"valor\":8.99}]")).andExpect(jsonPath("$.numero").value(3));
    }

    @Test
    void descontoSoComPermissaoEDentroDoLimiteConfigurado() throws Exception {
        String comDesconto = """
                {"itens":[{"produtoId":%d,"quantidade":10}],"descontoNaVenda":%s,
                 "pagamentos":[{"forma":"DINHEIRO","valor":100}]}""";

        vender(operadora, comDesconto.formatted(refrigerante, "5")).andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.detail").value("Você não tem permissão para dar desconto"));
        vender(operadoraComDesconto, comDesconto.formatted(refrigerante, "9")).andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.detail").value("O desconto passa do máximo permitido de 10%"));

        mvc.perform(put("/api/v1/vendas/configuracao").header("Authorization", admin)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"descontoMaximoPercentual\":15}"))
                .andExpect(jsonPath("$.descontoMaximoPercentual").value(15));
        vender(operadoraComDesconto, comDesconto.formatted(refrigerante, "9")).andExpect(status().isCreated())
                .andExpect(jsonPath("$.total").value(80.90));
    }

    @Test
    void regrasDosItens() throws Exception {
        Integer inativo = produto("OLD", "Produto antigo", "UN", "1", null, true);
        mvc.perform(delete("/api/v1/produtos/" + inativo).header("Authorization", admin));
        String umItem = "{\"itens\":[%s],\"pagamentos\":[{\"forma\":\"DINHEIRO\",\"valor\":100}]}";

        vender(operadora, umItem.formatted("{\"produtoId\":%d,\"quantidade\":1}".formatted(inativo)))
                .andExpect(status().isUnprocessableEntity());
        vender(operadora, umItem.formatted("{\"produtoId\":%d,\"quantidade\":1.5}".formatted(refrigerante)))
                .andExpect(status().isUnprocessableEntity());
        vender(operadora, umItem.formatted("{\"quantidade\":1}")).andExpect(status().isUnprocessableEntity());
        vender(operadora, umItem.formatted("{\"codigoDeBarras\":\"7891000315508\",\"quantidade\":1}"))
                .andExpect(status().isUnprocessableEntity());
        vender(operadora, "{\"itens\":[],\"pagamentos\":[]}").andExpect(status().isBadRequest());
    }

    @Test
    void pagamentosInvalidos() throws Exception {
        vender(operadora, umRefrigerante("[{\"forma\":\"DINHEIRO\",\"valor\":5}]"))
                .andExpect(status().isUnprocessableEntity());
        vender(operadora, umRefrigerante("[{\"forma\":\"CARTAO_CREDITO\",\"valor\":10}]"))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    void semCaixaAbertoNaoVende() throws Exception {
        cenario.vincular(cenario.usuario("caio@a.com"), empresaA, cenario.perfil(empresaA, "Caixa 2", CAIXA_OPERAR));
        String semCaixa = Login.bearer(mvc, "caio@a.com", empresaA.id());

        vender(semCaixa, umRefrigerante("[{\"forma\":\"DINHEIRO\",\"valor\":10}]"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.detail").value("Abra o seu caixa antes de vender"));
    }

    @Test
    void precoDaVendaNaoMudaQuandoOCadastroMuda() throws Exception {
        String local = vender(operadora, umRefrigerante("[{\"forma\":\"DINHEIRO\",\"valor\":10}]"))
                .andReturn().getResponse().getHeader("Location");
        jdbc.update("UPDATE produto SET preco_venda = 12.00 WHERE id = ?", refrigerante);

        mvc.perform(get(local).header("Authorization", operadora))
                .andExpect(jsonPath("$.itens[0].precoUnitario").value(8.99));
    }

    @Test
    void outraEmpresaNaoVeAVendaNemVendeProdutoAlheio() throws Exception {
        String local = vender(operadora, umRefrigerante("[{\"forma\":\"DINHEIRO\",\"valor\":10}]"))
                .andReturn().getResponse().getHeader("Location");

        mvc.perform(get(local).header("Authorization", adminDaB)).andExpect(status().isNotFound());
        abrirCaixa(adminDaB, "0");
        vender(adminDaB, umRefrigerante("[{\"forma\":\"DINHEIRO\",\"valor\":10}]")).andExpect(status().isNotFound());
    }

    // ---------- cancelamento ----------

    @Test
    void cancelamentoDevolveOEstoqueETiraOValorDoCaixa() throws Exception {
        Integer venda = idDe(vender(operadora, """
                {"itens":[{"produtoId":%d,"quantidade":3},{"produtoId":%d,"quantidade":1.5}],
                 "pagamentos":[{"forma":"DINHEIRO","valor":110}]}""".formatted(refrigerante, queijo)));

        cancelar(admin, venda, "Cliente desistiu").andExpect(status().isOk())
                .andExpect(jsonPath("$.situacao").value("CANCELADA"))
                .andExpect(jsonPath("$.motivoDoCancelamento").value("Cliente desistiu"));

        saldo(refrigerante).andExpect(jsonPath("$.quantidade").value(10));
        saldo(queijo).andExpect(jsonPath("$.quantidade").value(5));
        mvc.perform(get("/api/v1/estoque/produtos/" + refrigerante + "/extrato").header("Authorization", admin))
                .andExpect(jsonPath("$.itens[0].tipo").value("ESTORNO_DE_SAIDA"));
        mvc.perform(get("/api/v1/caixas/meu").header("Authorization", operadora))
                .andExpect(jsonPath("$.esperado.DINHEIRO").value(100.0));
    }

    @Test
    void cancelaUmaVezSoEComPermissao() throws Exception {
        Integer venda = idDe(vender(operadora, umRefrigerante("[{\"forma\":\"PIX\",\"valor\":8.99}]")));

        cancelar(operadora, venda, "Erro").andExpect(status().isForbidden());
        cancelar(admin, venda, "").andExpect(status().isBadRequest());
        cancelar(admin, venda, "Erro de digitação").andExpect(status().isOk());
        cancelar(admin, venda, "De novo").andExpect(status().isUnprocessableEntity());
        saldo(refrigerante).andExpect(jsonPath("$.quantidade").value(10));
    }

    @Test
    void depoisDoFechamentoDoCaixaNaoCancela() throws Exception {
        Integer venda = idDe(vender(operadora, umRefrigerante("[{\"forma\":\"PIX\",\"valor\":8.99}]")));
        Integer caixa = JsonPath.read(mvc.perform(get("/api/v1/caixas/meu").header("Authorization", operadora))
                .andReturn().getResponse().getContentAsString(), "$.id");
        mvc.perform(post("/api/v1/caixas/" + caixa + "/fechar").header("Authorization", operadora)
                .contentType(MediaType.APPLICATION_JSON).content("{\"contado\":{}}")).andExpect(status().isOk());

        cancelar(admin, venda, "Tarde demais").andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.detail").value(
                        "O caixa desta venda já foi fechado; o cancelamento não é mais possível (use devolução)"));
    }

    @Test
    void outraEmpresaNaoCancela() throws Exception {
        Integer venda = idDe(vender(operadora, umRefrigerante("[{\"forma\":\"PIX\",\"valor\":8.99}]")));

        cancelar(adminDaB, venda, "Invasão").andExpect(status().isNotFound());
    }

    // ---------- consultas ----------

    @Test
    void resumoDoDiaComTicketMedioFormasECancelamentos() throws Exception {
        vender(operadora, umRefrigerante("[{\"forma\":\"DINHEIRO\",\"valor\":10}]"));
        vender(operadoraComDesconto, """
                {"itens":[{"produtoId":%d,"quantidade":2}],"descontoNaVenda":0.98,
                 "pagamentos":[{"forma":"CARTAO_CREDITO","valor":17}]}""".formatted(refrigerante));
        Integer cancelada = idDe(vender(operadora, umRefrigerante("[{\"forma\":\"PIX\",\"valor\":8.99}]")));
        cancelar(admin, cancelada, "Erro");

        mvc.perform(get("/api/v1/vendas/resumo?data=" + hoje()).header("Authorization", admin))
                .andExpect(jsonPath("$.quantidade").value(2))
                .andExpect(jsonPath("$.total").value(25.99))
                .andExpect(jsonPath("$.descontos").value(0.98))
                .andExpect(jsonPath("$.ticketMedio").value(13.0))
                .andExpect(jsonPath("$.recebidoPorForma.DINHEIRO").value(8.99))
                .andExpect(jsonPath("$.recebidoPorForma.CARTAO_CREDITO").value(17.0))
                .andExpect(jsonPath("$.cancelamentos").value(1))
                .andExpect(jsonPath("$.totalCancelado").value(8.99));

        mvc.perform(get("/api/v1/vendas/resumo?data=" + hoje().minusDays(1)).header("Authorization", admin))
                .andExpect(jsonPath("$.quantidade").value(0))
                .andExpect(jsonPath("$.ticketMedio").value(0));
    }

    @Test
    void pesquisaPorCaixaSituacaoEDia() throws Exception {
        vender(operadora, umRefrigerante("[{\"forma\":\"PIX\",\"valor\":8.99}]"));
        Integer cancelada = idDe(vender(operadora, umRefrigerante("[{\"forma\":\"PIX\",\"valor\":8.99}]")));
        vender(operadoraComDesconto, umRefrigerante("[{\"forma\":\"PIX\",\"valor\":8.99}]"));
        cancelar(admin, cancelada, "Erro");
        Integer caixaDaAna = JsonPath.read(mvc.perform(get("/api/v1/caixas/meu").header("Authorization", operadora))
                .andReturn().getResponse().getContentAsString(), "$.id");

        pesquisar(admin, "?data=" + hoje()).andExpect(jsonPath("$.itens[*].numero", contains(3, 2, 1)));
        pesquisar(admin, "?caixaId=" + caixaDaAna + "&situacao=FINALIZADA")
                .andExpect(jsonPath("$.itens[*].numero", contains(1)));
        pesquisar(adminDaB, "").andExpect(jsonPath("$.itens", hasSize(0)));
        pesquisar(operadora, "").andExpect(status().isForbidden());
    }

    private ResultActions cancelar(String token, Integer venda, String motivo) throws Exception {
        return mvc.perform(post("/api/v1/vendas/" + venda + "/cancelar").header("Authorization", token)
                .contentType(MediaType.APPLICATION_JSON).content("{\"motivo\":\"" + motivo + "\"}"));
    }

    private ResultActions pesquisar(String token, String parametros) throws Exception {
        return mvc.perform(get("/api/v1/vendas" + parametros).header("Authorization", token));
    }

    private LocalDate hoje() {
        return LocalDate.ofInstant(relogio.instant(), ZoneId.of("America/Sao_Paulo"));
    }

    private static Integer idDe(ResultActions resposta) throws Exception {
        return JsonPath.read(resposta.andExpect(status().isCreated()).andReturn().getResponse().getContentAsString(),
                "$.id");
    }

    private String umRefrigerante(String pagamentos) {
        return "{\"itens\":[{\"produtoId\":%d,\"quantidade\":1}],\"pagamentos\":%s}".formatted(refrigerante, pagamentos);
    }

    private ResultActions vender(String token, String corpo) throws Exception {
        return mvc.perform(post("/api/v1/vendas").header("Authorization", token).contentType(MediaType.APPLICATION_JSON)
                .content(corpo));
    }

    private ResultActions saldo(Integer produto) throws Exception {
        return mvc.perform(get("/api/v1/estoque/saldos/" + produto).header("Authorization", admin));
    }

    private void abrirCaixa(String token, String troco) throws Exception {
        mvc.perform(post("/api/v1/caixas").header("Authorization", token).contentType(MediaType.APPLICATION_JSON)
                .content("{\"trocoInicial\":" + troco + "}")).andExpect(status().isCreated());
    }

    private void entrada(Integer produto, String quantidade) throws Exception {
        mvc.perform(post("/api/v1/estoque/entradas").header("Authorization", admin).contentType(MediaType.APPLICATION_JSON)
                .content("{\"produtoId\":%d,\"quantidade\":%s,\"custoUnitario\":5}".formatted(produto, quantidade)))
                .andExpect(status().isCreated());
    }

    private Integer produto(String codigo, String descricao, String sigla, String preco, String gtin,
            boolean controlaEstoque) throws Exception {
        Long unidade = jdbc.queryForObject("SELECT id FROM unidade_medida WHERE empresa_id = ? AND sigla = ?", Long.class,
                empresaA.id(), sigla);
        String resposta = mvc.perform(post("/api/v1/produtos").header("Authorization", admin)
                        .contentType(MediaType.APPLICATION_JSON).content("""
                                {"codigo":"%s","descricao":"%s","unidadeId":%d,"precoDeVenda":%s,"ncm":"22021000",
                                 "codigoDeBarras":%s,"controlaEstoque":%s}""".formatted(codigo, descricao, unidade,
                                new BigDecimal(preco), gtin == null ? "null" : "\"" + gtin + "\"", controlaEstoque)))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        return JsonPath.read(resposta, "$.id");
    }
}
