package br.com.goerp.financeiro.web;

import static br.com.goerp.usuario.dominio.Permissao.FINANCEIRO_LER;
import static br.com.goerp.usuario.dominio.Permissao.TITULO_GERENCIAR;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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

class ContasAPagarApiTest extends TesteDeIntegracao {

    @Autowired
    MockMvc mvc;

    @Autowired
    Cenario cenario;

    Empresa empresaA;
    String admin;
    String adminDaB;
    Integer banco;
    Integer fornecedor;
    Integer energia;

    @BeforeEach
    void preparar() throws Exception {
        empresaA = cenario.empresa("11222333000181", "Mercado A LTDA");
        Empresa empresaB = cenario.empresa("12345678000195", "Mercado B LTDA");
        cenario.vincular(cenario.usuario("admin@a.com"), empresaA);
        cenario.vincular(cenario.usuario("admin@b.com"), empresaB);
        admin = Login.bearer(mvc, "admin@a.com", empresaA.id());
        adminDaB = Login.bearer(mvc, "admin@b.com", empresaB.id());
        banco = idDe(enviar(post("/api/v1/contas-financeiras"), admin, "{\"tipo\":\"BANCO\",\"nome\":\"Banco\"}"));
        enviar(post("/api/v1/lancamentos-financeiros"), admin, """
                {"contaId":%d,"sentido":"ENTRADA","valor":1000,"forma":"TRANSFERENCIA","descricao":"Saldo inicial"}"""
                .formatted(banco)).andExpect(status().isCreated());
        fornecedor = idDe(enviar(post("/api/v1/fornecedores"), admin,
                "{\"documento\":\"11444777000161\",\"nome\":\"Companhia de Energia\"}"));
        energia = idDe(enviar(post("/api/v1/categorias-financeiras"), admin, "{\"tipo\":\"DESPESA\",\"nome\":\"Energia\"}"));
    }

    @Test
    void parceladoViraUmaContaPorParcelaComOsCentavosNaPrimeira() throws Exception {
        lancar(fornecedor, "Reforma da loja", null, "100", hoje().plusDays(10), 3).andExpect(status().isCreated())
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[0].valor").value(33.34))
                .andExpect(jsonPath("$[1].valor").value(33.33))
                .andExpect(jsonPath("$[1].parcela").value(2))
                .andExpect(jsonPath("$[1].parcelas").value(3))
                .andExpect(jsonPath("$[1].vencimento").value(hoje().plusDays(10).plusMonths(1).toString()))
                .andExpect(jsonPath("$[0].fornecedorId").value(fornecedor))
                .andExpect(jsonPath("$[0].formaPrevista").value("BOLETO"))
                .andExpect(jsonPath("$[0].situacao").value("ABERTO"));
    }

    @Test
    void pagamentoParcialEDepoisComJurosTiramDaContaComAFormaUsada() throws Exception {
        Integer conta = primeira(lancar(fornecedor, "Energia de novembro", energia, "100", hoje(), 1));

        pagar(conta, "PIX", "40", null, null).andExpect(status().isOk())
                .andExpect(jsonPath("$.situacao").value("PARCIALMENTE_PAGO"))
                .andExpect(jsonPath("$.saldoDevedor").value(60))
                .andExpect(jsonPath("$.pagamentos[0].forma").value("PIX"))
                .andExpect(jsonPath("$.pagamentos[0].contaId").value(banco));
        pagar(conta, "BOLETO", "63", "2", "1").andExpect(jsonPath("$.situacao").value("QUITADO"))
                .andExpect(jsonPath("$.saldoDevedor").value(0))
                .andExpect(jsonPath("$.totalPago").value(103));

        saldoDoBanco().andExpect(jsonPath("$.saldo").value(897));
        extrato("").andExpect(jsonPath("$.itens[0].forma").value("BOLETO"))
                .andExpect(jsonPath("$.itens[0].valor").value(63))
                .andExpect(jsonPath("$.itens[0].origem").value("BAIXA_DE_TITULO"))
                .andExpect(jsonPath("$.itens[0].categoriaId").value(energia))
                .andExpect(jsonPath("$.itens[0].descricao").value("Pagamento: Energia de novembro"))
                .andExpect(jsonPath("$.itens[1].forma").value("PIX"));
        extrato("?forma=PIX").andExpect(jsonPath("$.itens", hasSize(1)));
        pagar(conta, "PIX", "1", null, null).andExpect(status().isUnprocessableEntity());
    }

    @Test
    void semSaldoNaContaNadaEhGravadoNemNoTitulo() throws Exception {
        Integer conta = primeira(lancar(fornecedor, "Equipamento", null, "1500", hoje(), 1));

        pagar(conta, "TRANSFERENCIA", "1500", null, null).andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.detail").value("Saldo insuficiente na conta: disponível 1000.00, pedido 1500.00"));

        contaAPagar(conta).andExpect(jsonPath("$.situacao").value("ABERTO"))
                .andExpect(jsonPath("$.pagamentos", hasSize(0)));
        assertThat(jdbc.queryForObject("SELECT count(*) FROM baixa_titulo", Integer.class)).isZero();
        saldoDoBanco().andExpect(jsonPath("$.saldo").value(1000));
    }

    @Test
    void recusaPagarAMaisOuAPrazo() throws Exception {
        Integer conta = primeira(lancar(fornecedor, "Aluguel", null, "100", hoje(), 1));

        pagar(conta, "PIX", "100.01", null, null).andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.detail").value("O pagamento abate 100.01, mas o título só deve 100.00"));
        pagar(conta, "A_PRAZO", "10", null, null).andExpect(status().isUnprocessableEntity());
        saldoDoBanco().andExpect(jsonPath("$.saldo").value(1000));
    }

    @Test
    void estornoDoPagamentoDevolveODevidoEODinheiroUmaVezSo() throws Exception {
        Integer conta = primeira(lancar(fornecedor, "Aluguel", null, "100", hoje(), 1));
        pagar(conta, "PIX", "100", null, null);

        estornarPagamento(conta, 1, "Pago em duplicidade").andExpect(status().isOk())
                .andExpect(jsonPath("$.situacao").value("ABERTO"))
                .andExpect(jsonPath("$.saldoDevedor").value(100))
                .andExpect(jsonPath("$.pagamentos[0].estornado").value(true));

        saldoDoBanco().andExpect(jsonPath("$.saldo").value(1000));
        extrato("").andExpect(jsonPath("$.itens[0].tipo").value("ESTORNO_DE_SAIDA"))
                .andExpect(jsonPath("$.itens[0].forma").value("PIX"));
        estornarPagamento(conta, 1, "De novo").andExpect(status().isUnprocessableEntity());
        Integer lancamentoDoPagamento = jdbc.queryForObject(
                "SELECT id FROM lancamento_financeiro WHERE origem = 'BAIXA_DE_TITULO' AND estornado_id IS NULL",
                Integer.class);
        enviar(post("/api/v1/lancamentos-financeiros/%d/estornar".formatted(lancamentoDoPagamento)), admin,
                "{\"motivo\":\"Pelo extrato\"}").andExpect(status().isUnprocessableEntity());
    }

    @Test
    void cancelaEAlteraSoSemPagamentoComControleDeVersao() throws Exception {
        Integer conta = primeira(lancar(fornecedor, "Aluguel", null, "100", hoje(), 1));

        alterar(conta, "Aluguel de dezembro", "120", 0).andExpect(status().isOk())
                .andExpect(jsonPath("$.valor").value(120)).andExpect(jsonPath("$.saldoDevedor").value(120))
                .andExpect(jsonPath("$.versao").value(1));
        alterar(conta, "Outra", "130", 0).andExpect(status().isConflict());

        pagar(conta, "PIX", "10", null, null);
        alterar(conta, "Outra", "130", 2).andExpect(status().isUnprocessableEntity());
        cancelar(conta).andExpect(status().isUnprocessableEntity());

        estornarPagamento(conta, 1, "Errado");
        cancelar(conta).andExpect(status().isOk()).andExpect(jsonPath("$.situacao").value("CANCELADO"))
                .andExpect(jsonPath("$.saldoDevedor").value(0));
        pagar(conta, "PIX", "10", null, null).andExpect(status().isUnprocessableEntity());
    }

    @Test
    void categoriaDeReceitaNaoServeParaContaAPagar() throws Exception {
        Integer vendas = idDe(enviar(post("/api/v1/categorias-financeiras"), admin,
                "{\"tipo\":\"RECEITA\",\"nome\":\"Vendas\"}"));

        lancar(fornecedor, "Errada", vendas, "10", hoje(), 1).andExpect(status().isUnprocessableEntity());
        lancar(fornecedor, "Sem vencimento", null, "10", null, 1).andExpect(status().isUnprocessableEntity());
        lancar(fornecedor, "Muitas parcelas", null, "1000", hoje(), 61).andExpect(status().isUnprocessableEntity());
        assertThat(jdbc.queryForObject("SELECT count(*) FROM titulo", Integer.class)).isZero();
    }

    @Test
    void vencidasEResumoPelaDataDeHoje() throws Exception {
        Integer vencida = primeira(lancar(null, "Vencida", null, "10", hoje().minusDays(3), 1));
        lancar(null, "Vence hoje", null, "20", hoje(), 1);
        lancar(null, "Vence amanhã", null, "30", hoje().plusDays(1), 1);
        Integer paga = primeira(lancar(null, "Já paga", null, "40", hoje().minusDays(5), 1));
        pagar(paga, "DINHEIRO", "40", null, null);

        mvc.perform(get("/api/v1/contas-a-pagar?vencidas=true").header("Authorization", admin))
                .andExpect(jsonPath("$.itens", hasSize(1)))
                .andExpect(jsonPath("$.itens[0].id").value(vencida))
                .andExpect(jsonPath("$.itens[0].vencido").value(true))
                .andExpect(jsonPath("$.itens[0].diasEmAtraso").value(3));
        mvc.perform(get("/api/v1/contas-a-pagar?emAberto=true").header("Authorization", admin))
                .andExpect(jsonPath("$.itens", hasSize(3)))
                .andExpect(jsonPath("$.itens[0].descricao").value("Vencida"));
        mvc.perform(get("/api/v1/contas-a-pagar?situacao=QUITADO").header("Authorization", admin))
                .andExpect(jsonPath("$.itens", hasSize(1)));
        mvc.perform(get("/api/v1/contas-a-pagar/resumo").header("Authorization", admin))
                .andExpect(jsonPath("$.emAberto.quantidade").value(3))
                .andExpect(jsonPath("$.emAberto.valor").value(60))
                .andExpect(jsonPath("$.vencidos.quantidade").value(1))
                .andExpect(jsonPath("$.vencidos.valor").value(10))
                .andExpect(jsonPath("$.vencemHoje.valor").value(20));
    }

    @Test
    void outraEmpresaNaoVeNemPagaNemUsaFornecedorOuContaAlheios() throws Exception {
        Integer conta = primeira(lancar(fornecedor, "Aluguel", null, "100", hoje(), 1));
        Integer contaDaB = primeira(enviar(post("/api/v1/contas-a-pagar"), adminDaB, corpo(null, "Da B", null, "10",
                hoje(), 1)));

        mvc.perform(get("/api/v1/contas-a-pagar/" + conta).header("Authorization", adminDaB))
                .andExpect(status().isNotFound());
        mvc.perform(get("/api/v1/contas-a-pagar").header("Authorization", adminDaB))
                .andExpect(jsonPath("$.itens", hasSize(1)));
        enviar(post("/api/v1/contas-a-pagar"), adminDaB, corpo(fornecedor, "Com fornecedor da A", null, "10", hoje(), 1))
                .andExpect(status().isNotFound());
        enviar(post("/api/v1/contas-a-pagar/%d/pagamentos".formatted(conta)), adminDaB, baixa("PIX", "10", null, null))
                .andExpect(status().isNotFound());
        // a B não pode tirar dinheiro do banco da A, nem pagando uma conta dela mesma
        enviar(post("/api/v1/contas-a-pagar/%d/pagamentos".formatted(contaDaB)), adminDaB, baixa("PIX", "10", null, null))
                .andExpect(status().isNotFound());
        saldoDoBanco().andExpect(jsonPath("$.saldo").value(1000));
    }

    @Test
    void quemLancaContasNaoPrecisaPoderPagar() throws Exception {
        cenario.vincular(cenario.usuario("auxiliar@a.com"), empresaA,
                cenario.perfil(empresaA, "Auxiliar", FINANCEIRO_LER, TITULO_GERENCIAR));
        String auxiliar = Login.bearer(mvc, "auxiliar@a.com", empresaA.id());

        Integer conta = primeira(enviar(post("/api/v1/contas-a-pagar"), auxiliar,
                corpo(fornecedor, "Aluguel", null, "100", hoje(), 1)).andExpect(status().isCreated()));
        enviar(post("/api/v1/contas-a-pagar/%d/pagamentos".formatted(conta)), auxiliar, baixa("PIX", "10", null, null))
                .andExpect(status().isForbidden());
    }

    private ResultActions lancar(Integer fornecedorId, String descricao, Integer categoria, String valor,
            LocalDate vencimento, int parcelas) throws Exception {
        return enviar(post("/api/v1/contas-a-pagar"), admin,
                corpo(fornecedorId, descricao, categoria, valor, vencimento, parcelas));
    }

    private static String corpo(Integer fornecedorId, String descricao, Integer categoria, String valor,
            LocalDate vencimento, int parcelas) {
        return """
                {"fornecedorId":%s,"descricao":"%s","categoriaId":%s,"formaPrevista":"BOLETO",
                 "condicao":{"valor":%s,"primeiroVencimento":%s,"parcelas":%d}}"""
                .formatted(fornecedorId, descricao, categoria, valor,
                        vencimento == null ? null : "\"" + vencimento + "\"", parcelas);
    }

    private ResultActions pagar(Integer conta, String forma, String valor, String juros, String multa)
            throws Exception {
        return enviar(post("/api/v1/contas-a-pagar/%d/pagamentos".formatted(conta)), admin,
                baixa(forma, valor, juros, multa));
    }

    private String baixa(String forma, String valor, String juros, String multa) {
        return "{\"contaId\":%d,\"forma\":\"%s\",\"valorPago\":%s,\"juros\":%s,\"multa\":%s}"
                .formatted(banco, forma, valor, juros, multa);
    }

    private ResultActions estornarPagamento(Integer conta, int numero, String motivo) throws Exception {
        return enviar(post("/api/v1/contas-a-pagar/%d/pagamentos/%d/estornar".formatted(conta, numero)), admin,
                "{\"motivo\":\"%s\"}".formatted(motivo));
    }

    private ResultActions alterar(Integer conta, String descricao, String valor, int versao) throws Exception {
        return enviar(put("/api/v1/contas-a-pagar/" + conta), admin,
                "{\"descricao\":\"%s\",\"valor\":%s,\"vencimento\":\"%s\",\"versao\":%d}"
                        .formatted(descricao, valor, hoje(), versao));
    }

    private ResultActions cancelar(Integer conta) throws Exception {
        return enviar(post("/api/v1/contas-a-pagar/%d/cancelar".formatted(conta)), admin,
                "{\"motivo\":\"Lançada em duplicidade\"}");
    }

    private ResultActions contaAPagar(Integer conta) throws Exception {
        return mvc.perform(get("/api/v1/contas-a-pagar/" + conta).header("Authorization", admin));
    }

    private ResultActions saldoDoBanco() throws Exception {
        return mvc.perform(get("/api/v1/contas-financeiras/" + banco).header("Authorization", admin));
    }

    private ResultActions extrato(String filtro) throws Exception {
        return mvc.perform(get("/api/v1/contas-financeiras/%d/extrato%s".formatted(banco, filtro))
                .header("Authorization", admin));
    }

    private LocalDate hoje() {
        return LocalDate.ofInstant(relogio.instant(), ZoneId.of("America/Sao_Paulo"));
    }

    private ResultActions enviar(MockHttpServletRequestBuilder requisicao, String token, String corpo) throws Exception {
        return mvc.perform(requisicao.header("Authorization", token).contentType(MediaType.APPLICATION_JSON).content(corpo));
    }

    private static Integer primeira(ResultActions resposta) throws Exception {
        return JsonPath.read(resposta.andReturn().getResponse().getContentAsString(), "$[0].id");
    }

    private static Integer idDe(ResultActions resposta) throws Exception {
        return JsonPath.read(resposta.andReturn().getResponse().getContentAsString(), "$.id");
    }
}
