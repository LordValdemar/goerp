package br.com.goerp.financeiro.web;

import static br.com.goerp.usuario.dominio.Permissao.FINANCEIRO_LER;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hamcrest.Matchers.hasSize;
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

class ContasELancamentosApiTest extends TesteDeIntegracao {

    @Autowired
    MockMvc mvc;

    @Autowired
    Cenario cenario;

    Empresa empresaA;
    String admin;
    String adminDaB;
    Integer tesouraria;
    Integer banco;

    @BeforeEach
    void preparar() throws Exception {
        empresaA = cenario.empresa("11222333000181", "Mercado A LTDA");
        Empresa empresaB = cenario.empresa("12345678000195", "Mercado B LTDA");
        cenario.vincular(cenario.usuario("admin@a.com"), empresaA);
        cenario.vincular(cenario.usuario("admin@b.com"), empresaB);
        admin = Login.bearer(mvc, "admin@a.com", empresaA.id());
        adminDaB = Login.bearer(mvc, "admin@b.com", empresaB.id());
        tesouraria = conta("CAIXA", "Tesouraria", false);
        banco = conta("BANCO", "Banco do Bairro", false);
    }

    @Test
    void cadaLancamentoFicaMarcadoComAFormaEOExtratoFiltraPorEla() throws Exception {
        Integer aluguel = categoria("DESPESA", "Aluguel");
        lancar(tesouraria, "ENTRADA", "500", "DINHEIRO", null, "Saldo inicial").andExpect(status().isCreated())
                .andExpect(jsonPath("$.forma").value("DINHEIRO"))
                .andExpect(jsonPath("$.origem").value("MANUAL"));
        lancar(tesouraria, "SAIDA", "120.50", "PIX", aluguel, "Aluguel de novembro").andExpect(status().isCreated())
                .andExpect(jsonPath("$.saldoResultante").value(379.50))
                .andExpect(jsonPath("$.categoriaId").value(aluguel));

        conta(tesouraria).andExpect(jsonPath("$.saldo").value(379.50));
        extrato(tesouraria, "").andExpect(jsonPath("$.itens", hasSize(2)))
                .andExpect(jsonPath("$.itens[0].forma").value("PIX"))
                .andExpect(jsonPath("$.itens[1].forma").value("DINHEIRO"));
        extrato(tesouraria, "?forma=PIX").andExpect(jsonPath("$.itens", hasSize(1)))
                .andExpect(jsonPath("$.itens[0].descricao").value("Aluguel de novembro"));
    }

    @Test
    void extratoFiltraPorPeriodo() throws Exception {
        lancar(tesouraria, "ENTRADA", "10", "DINHEIRO", null, "Hoje");
        relogio.avancar(Duration.ofDays(2));
        lancar(tesouraria, "ENTRADA", "20", "DINHEIRO", null, "Depois");
        String hoje = hoje().toString();

        extrato(tesouraria, "?de=" + hoje).andExpect(jsonPath("$.itens", hasSize(1)))
                .andExpect(jsonPath("$.itens[0].descricao").value("Depois"));
        extrato(tesouraria, "?ate=" + hoje().minusDays(1)).andExpect(jsonPath("$.itens", hasSize(1)))
                .andExpect(jsonPath("$.itens[0].descricao").value("Hoje"));
        extrato(tesouraria, "?de=%s&ate=%s".formatted(hoje, hoje().minusDays(1)))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    void recusaAPrazoSaldoInsuficienteECategoriaDoOutroTipoSemGravarNada() throws Exception {
        Integer vendas = categoria("RECEITA", "Vendas");
        lancar(tesouraria, "ENTRADA", "50", "DINHEIRO", null, "Troco");

        lancar(tesouraria, "ENTRADA", "10", "A_PRAZO", null, "Fiado").andExpect(status().isUnprocessableEntity());
        lancar(tesouraria, "SAIDA", "50.01", "DINHEIRO", null, "Sangria").andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.detail").value("Saldo insuficiente na conta: disponível 50.00, pedido 50.01"));
        lancar(tesouraria, "SAIDA", "5", "DINHEIRO", vendas, "Despesa na categoria errada")
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.detail").value("A categoria Vendas é de receita e não serve para este lançamento"));
        lancar(tesouraria, "SAIDA", "5", "DINHEIRO", 9999, "Categoria inexistente").andExpect(status().isNotFound());
        lancar(tesouraria, "SAIDA", "5.001", "DINHEIRO", null, "Centavo fracionado")
                .andExpect(status().isUnprocessableEntity());

        conta(tesouraria).andExpect(jsonPath("$.saldo").value(50));
        assertThat(jdbc.queryForObject("SELECT count(*) FROM lancamento_financeiro", Integer.class)).isEqualTo(1);
    }

    @Test
    void transferenciaLancaAsDuasPontasEOEstornoDesfazAsDuas() throws Exception {
        lancar(tesouraria, "ENTRADA", "300", "DINHEIRO", null, "Vendas do dia");

        String resposta = enviar(post("/api/v1/transferencias-financeiras"), admin, """
                {"contaOrigemId":%d,"contaDestinoId":%d,"valor":200,"forma":"DINHEIRO","descricao":"Depósito do cofre"}"""
                .formatted(tesouraria, banco)).andExpect(status().isCreated())
                .andExpect(jsonPath("$.saida.tipo").value("SAIDA"))
                .andExpect(jsonPath("$.entrada.tipo").value("ENTRADA"))
                .andExpect(jsonPath("$.entrada.forma").value("DINHEIRO"))
                .andExpect(jsonPath("$.entrada.origem").value("TRANSFERENCIA"))
                .andReturn().getResponse().getContentAsString();
        Integer entrada = JsonPath.read(resposta, "$.entrada.id");
        conta(tesouraria).andExpect(jsonPath("$.saldo").value(100));
        conta(banco).andExpect(jsonPath("$.saldo").value(200));

        estornar(entrada, "Depósito não compensou").andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(2)));

        conta(tesouraria).andExpect(jsonPath("$.saldo").value(300));
        conta(banco).andExpect(jsonPath("$.saldo").value(0));
        estornar(entrada, "De novo").andExpect(status().isUnprocessableEntity());
    }

    @Test
    void transferenciaSemFormaUsaTransferenciaENaoVaiParaAMesmaConta() throws Exception {
        lancar(banco, "ENTRADA", "100", "TRANSFERENCIA", null, "Aporte");

        transferir(banco, tesouraria, "30").andExpect(status().isCreated())
                .andExpect(jsonPath("$.saida.forma").value("TRANSFERENCIA"));
        transferir(banco, banco, "30").andExpect(status().isUnprocessableEntity());
        transferir(banco, tesouraria, "70.01").andExpect(status().isUnprocessableEntity());

        conta(banco).andExpect(jsonPath("$.saldo").value(70));
        conta(tesouraria).andExpect(jsonPath("$.saldo").value(30));
    }

    @Test
    void estornoDeAvulsoUmaVezSoComMotivo() throws Exception {
        Integer lancamento = idDe(lancar(tesouraria, "ENTRADA", "80", "CARTAO_DEBITO", null, "Lançado errado"));

        estornar(lancamento, " ").andExpect(status().isBadRequest());
        String estornos = estornar(lancamento, "Valor duplicado").andExpect(status().isOk())
                .andExpect(jsonPath("$[0].tipo").value("ESTORNO_DE_ENTRADA"))
                .andExpect(jsonPath("$[0].forma").value("CARTAO_DEBITO"))
                .andExpect(jsonPath("$[0].estornado").value(lancamento))
                .andReturn().getResponse().getContentAsString();
        Integer estorno = JsonPath.read(estornos, "$[0].id");

        estornar(lancamento, "De novo").andExpect(status().isUnprocessableEntity());
        estornar(estorno, "Estorno do estorno").andExpect(status().isUnprocessableEntity());
        conta(tesouraria).andExpect(jsonPath("$.saldo").value(0));
    }

    @Test
    void lancamentoNaoPodeSerAlteradoNemApagadoNemPeloBanco() throws Exception {
        lancar(tesouraria, "ENTRADA", "10", "DINHEIRO", null, "Teste");

        assertThatThrownBy(() -> jdbc.update("UPDATE lancamento_financeiro SET valor = 1"))
                .hasMessageContaining("não podem ser alterados");
        assertThatThrownBy(() -> jdbc.update("DELETE FROM lancamento_financeiro"))
                .hasMessageContaining("não podem ser alterados");
    }

    @Test
    void contaComNomeUnicoVersaoESoInativaZerada() throws Exception {
        criarConta("CAIXA", "tesouraria", false).andExpect(status().isUnprocessableEntity());
        lancar(tesouraria, "ENTRADA", "10", "DINHEIRO", null, "Troco");
        // o lançamento não muda a versão: quem estava editando o nome não leva 409 por causa dele
        alterarConta(tesouraria, "Cofre", 0).andExpect(status().isOk()).andExpect(jsonPath("$.versao").value(1))
                .andExpect(jsonPath("$.saldo").value(10));
        alterarConta(tesouraria, "Cofre da loja", 0).andExpect(status().isConflict());
        alterarConta(tesouraria, "Banco do bairro", 1).andExpect(status().isUnprocessableEntity());

        mvc.perform(delete("/api/v1/contas-financeiras/" + tesouraria).header("Authorization", admin))
                .andExpect(status().isUnprocessableEntity());
        lancar(tesouraria, "SAIDA", "10", "DINHEIRO", null, "Retirada");
        mvc.perform(delete("/api/v1/contas-financeiras/" + tesouraria).header("Authorization", admin))
                .andExpect(status().isNoContent());
        lancar(tesouraria, "ENTRADA", "1", "DINHEIRO", null, "Na conta inativa").andExpect(status().isUnprocessableEntity());
        mvc.perform(get("/api/v1/contas-financeiras?ativa=true").header("Authorization", admin))
                .andExpect(jsonPath("$", hasSize(1)));
        assertThat(jdbc.queryForObject("SELECT count(*) FROM conta_financeira_aud WHERE id = ?", Integer.class,
                tesouraria)).isGreaterThanOrEqualTo(3);
    }

    @Test
    void bancoComChequeEspecialPodeFicarNegativo() throws Exception {
        Integer especial = conta("BANCO", "Conta com limite", true);

        lancar(especial, "SAIDA", "150", "BOLETO", null, "Fornecedor").andExpect(status().isCreated())
                .andExpect(jsonPath("$.saldoResultante").value(-150));
    }

    @Test
    void categoriasPorTipoComNomeUnico() throws Exception {
        categoria("RECEITA", "Vendas");
        Integer energia = categoria("DESPESA", "Energia");
        enviar(post("/api/v1/categorias-financeiras"), admin, "{\"tipo\":\"DESPESA\",\"nome\":\"VENDAS\"}")
                .andExpect(status().isUnprocessableEntity());

        mvc.perform(get("/api/v1/categorias-financeiras?tipo=DESPESA").header("Authorization", admin))
                .andExpect(jsonPath("$", hasSize(1))).andExpect(jsonPath("$[0].nome").value("Energia"));
        mvc.perform(delete("/api/v1/categorias-financeiras/" + energia).header("Authorization", admin))
                .andExpect(status().isNoContent());
        lancar(tesouraria, "ENTRADA", "10", "DINHEIRO", null, "Troco");
        lancar(tesouraria, "SAIDA", "5", "DINHEIRO", energia, "Conta de luz").andExpect(status().isUnprocessableEntity());
    }

    @Test
    void outraEmpresaNaoVeNemMexe() throws Exception {
        Integer lancamento = idDe(lancar(tesouraria, "ENTRADA", "100", "DINHEIRO", null, "Saldo"));
        Integer contaDaB = idDe(enviar(post("/api/v1/contas-financeiras"), adminDaB,
                "{\"tipo\":\"CAIXA\",\"nome\":\"Caixa B\"}"));

        mvc.perform(get("/api/v1/contas-financeiras").header("Authorization", adminDaB))
                .andExpect(jsonPath("$", hasSize(1))).andExpect(jsonPath("$[0].nome").value("Caixa B"));
        mvc.perform(get("/api/v1/contas-financeiras/" + tesouraria).header("Authorization", adminDaB))
                .andExpect(status().isNotFound());
        mvc.perform(get("/api/v1/contas-financeiras/%d/extrato".formatted(tesouraria)).header("Authorization", adminDaB))
                .andExpect(status().isNotFound());
        mvc.perform(get("/api/v1/lancamentos-financeiros/" + lancamento).header("Authorization", adminDaB))
                .andExpect(status().isNotFound());
        enviar(post("/api/v1/lancamentos-financeiros"), adminDaB, avulso(tesouraria, "SAIDA", "1", "DINHEIRO", null, "x"))
                .andExpect(status().isNotFound());
        enviar(post("/api/v1/lancamentos-financeiros/%d/estornar".formatted(lancamento)), adminDaB,
                "{\"motivo\":\"x\"}").andExpect(status().isNotFound());
        enviar(post("/api/v1/transferencias-financeiras"), adminDaB, """
                {"contaOrigemId":%d,"contaDestinoId":%d,"valor":1,"descricao":"x"}""".formatted(tesouraria, contaDaB))
                .andExpect(status().isNotFound());
        conta(tesouraria).andExpect(jsonPath("$.saldo").value(100));
    }

    @Test
    void quemSoConsultaNaoLancaNemCadastra() throws Exception {
        cenario.vincular(cenario.usuario("contador@a.com"), empresaA, cenario.perfil(empresaA, "Contador", FINANCEIRO_LER));
        String contador = Login.bearer(mvc, "contador@a.com", empresaA.id());

        mvc.perform(get("/api/v1/contas-financeiras/%d/extrato".formatted(tesouraria)).header("Authorization", contador))
                .andExpect(status().isOk());
        enviar(post("/api/v1/lancamentos-financeiros"), contador, avulso(tesouraria, "ENTRADA", "1", "DINHEIRO", null, "x"))
                .andExpect(status().isForbidden());
        enviar(post("/api/v1/contas-financeiras"), contador, "{\"tipo\":\"CAIXA\",\"nome\":\"Outra\"}")
                .andExpect(status().isForbidden());
        enviar(post("/api/v1/categorias-financeiras"), contador, "{\"tipo\":\"RECEITA\",\"nome\":\"Outra\"}")
                .andExpect(status().isForbidden());
    }

    private ResultActions lancar(Integer conta, String sentido, String valor, String forma, Integer categoria,
            String descricao) throws Exception {
        return enviar(post("/api/v1/lancamentos-financeiros"), admin,
                avulso(conta, sentido, valor, forma, categoria, descricao));
    }

    private static String avulso(Integer conta, String sentido, String valor, String forma, Integer categoria,
            String descricao) {
        return "{\"contaId\":%d,\"sentido\":\"%s\",\"valor\":%s,\"forma\":\"%s\",\"categoriaId\":%s,\"descricao\":\"%s\"}"
                .formatted(conta, sentido, valor, forma, categoria, descricao);
    }

    private ResultActions transferir(Integer origem, Integer destino, String valor) throws Exception {
        return enviar(post("/api/v1/transferencias-financeiras"), admin, """
                {"contaOrigemId":%d,"contaDestinoId":%d,"valor":%s,"descricao":"Transferência"}"""
                .formatted(origem, destino, valor));
    }

    private ResultActions estornar(Integer lancamento, String motivo) throws Exception {
        return enviar(post("/api/v1/lancamentos-financeiros/%d/estornar".formatted(lancamento)), admin,
                "{\"motivo\":\"%s\"}".formatted(motivo));
    }

    private ResultActions conta(Integer id) throws Exception {
        return mvc.perform(get("/api/v1/contas-financeiras/" + id).header("Authorization", admin));
    }

    private ResultActions extrato(Integer conta, String filtro) throws Exception {
        return mvc.perform(get("/api/v1/contas-financeiras/%d/extrato%s".formatted(conta, filtro))
                .header("Authorization", admin));
    }

    private Integer conta(String tipo, String nome, boolean negativo) throws Exception {
        return idDe(criarConta(tipo, nome, negativo).andExpect(status().isCreated()));
    }

    private ResultActions criarConta(String tipo, String nome, boolean negativo) throws Exception {
        return enviar(post("/api/v1/contas-financeiras"), admin,
                "{\"tipo\":\"%s\",\"nome\":\"%s\",\"permiteSaldoNegativo\":%s}".formatted(tipo, nome, negativo));
    }

    private ResultActions alterarConta(Integer id, String nome, int versao) throws Exception {
        return enviar(put("/api/v1/contas-financeiras/" + id), admin,
                "{\"nome\":\"%s\",\"permiteSaldoNegativo\":false,\"versao\":%d}".formatted(nome, versao));
    }

    private Integer categoria(String tipo, String nome) throws Exception {
        return idDe(enviar(post("/api/v1/categorias-financeiras"), admin,
                "{\"tipo\":\"%s\",\"nome\":\"%s\"}".formatted(tipo, nome)).andExpect(status().isCreated()));
    }

    private LocalDate hoje() {
        return LocalDate.ofInstant(relogio.instant(), ZoneId.of("America/Sao_Paulo"));
    }

    private ResultActions enviar(MockHttpServletRequestBuilder requisicao, String token, String corpo) throws Exception {
        return mvc.perform(requisicao.header("Authorization", token).contentType(MediaType.APPLICATION_JSON).content(corpo));
    }

    private static Integer idDe(ResultActions resposta) throws Exception {
        return JsonPath.read(resposta.andReturn().getResponse().getContentAsString(), "$.id");
    }
}
