package br.com.goerp.financeiro.web;

import static br.com.goerp.usuario.dominio.Permissao.FINANCEIRO_LER;
import static br.com.goerp.usuario.dominio.Permissao.TITULO_BAIXAR;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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

class ContasAReceberApiTest extends TesteDeIntegracao {

    @Autowired
    MockMvc mvc;

    @Autowired
    Cenario cenario;

    Empresa empresaA;
    String admin;
    String adminDaB;
    Integer caixa;
    Integer cliente;
    Integer vendas;

    @BeforeEach
    void preparar() throws Exception {
        empresaA = cenario.empresa("11222333000181", "Mercado A LTDA");
        Empresa empresaB = cenario.empresa("12345678000195", "Mercado B LTDA");
        cenario.vincular(cenario.usuario("admin@a.com"), empresaA);
        cenario.vincular(cenario.usuario("admin@b.com"), empresaB);
        admin = Login.bearer(mvc, "admin@a.com", empresaA.id());
        adminDaB = Login.bearer(mvc, "admin@b.com", empresaB.id());
        caixa = idDe(enviar(post("/api/v1/contas-financeiras"), admin, "{\"tipo\":\"CAIXA\",\"nome\":\"Tesouraria\"}"));
        cliente = idDe(enviar(post("/api/v1/clientes"), admin, "{\"documento\":\"52998224725\",\"nome\":\"Maria Silva\"}"));
        vendas = idDe(enviar(post("/api/v1/categorias-financeiras"), admin, "{\"tipo\":\"RECEITA\",\"nome\":\"Vendas\"}"));
    }

    @Test
    void crediarioRecebidoEmPartesComFormasDiferentesEntraNaContaComCadaForma() throws Exception {
        Integer parcela = primeira(lancar(cliente, vendas, "150", hoje(), 1).andExpect(status().isCreated())
                .andExpect(jsonPath("$[0].clienteId").value(cliente))
                .andExpect(jsonPath("$[0].fornecedorId").doesNotExist())
                .andExpect(jsonPath("$[0].natureza").value("A_RECEBER"))
                .andExpect(jsonPath("$[0].formaPrevista").value("A_PRAZO")));

        receber(parcela, "DINHEIRO", "50").andExpect(status().isOk())
                .andExpect(jsonPath("$.situacao").value("PARCIALMENTE_PAGO"))
                .andExpect(jsonPath("$.pagamentos[0].forma").value("DINHEIRO"));
        receber(parcela, "PIX", "100").andExpect(jsonPath("$.situacao").value("QUITADO"))
                .andExpect(jsonPath("$.pagamentos[1].forma").value("PIX"));

        mvc.perform(get("/api/v1/contas-financeiras/" + caixa).header("Authorization", admin))
                .andExpect(jsonPath("$.saldo").value(150));
        mvc.perform(get("/api/v1/contas-financeiras/%d/extrato".formatted(caixa)).header("Authorization", admin))
                .andExpect(jsonPath("$.itens[0].tipo").value("ENTRADA"))
                .andExpect(jsonPath("$.itens[0].forma").value("PIX"))
                .andExpect(jsonPath("$.itens[0].categoriaId").value(vendas))
                .andExpect(jsonPath("$.itens[0].descricao").value("Recebimento: Crediário"))
                .andExpect(jsonPath("$.itens[1].forma").value("DINHEIRO"));
    }

    @Test
    void estornoDoRecebimentoPrecisaDoDinheiroAindaNaConta() throws Exception {
        Integer parcela = primeira(lancar(cliente, null, "100", hoje(), 1));
        receber(parcela, "DINHEIRO", "100");
        enviar(post("/api/v1/lancamentos-financeiros"), admin, """
                {"contaId":%d,"sentido":"SAIDA","valor":30,"forma":"DINHEIRO","descricao":"Sangria para o cofre"}"""
                .formatted(caixa)).andExpect(status().isCreated());

        estornar(parcela).andExpect(status().isUnprocessableEntity());
        titulo(parcela).andExpect(jsonPath("$.situacao").value("QUITADO"))
                .andExpect(jsonPath("$.pagamentos[0].estornado").value(false));

        enviar(post("/api/v1/lancamentos-financeiros"), admin, """
                {"contaId":%d,"sentido":"ENTRADA","valor":30,"forma":"DINHEIRO","descricao":"Volta do cofre"}"""
                .formatted(caixa));
        estornar(parcela).andExpect(status().isOk()).andExpect(jsonPath("$.situacao").value("ABERTO"));
    }

    @Test
    void exigeClienteECategoriaDeReceita() throws Exception {
        Integer aluguel = idDe(enviar(post("/api/v1/categorias-financeiras"), admin,
                "{\"tipo\":\"DESPESA\",\"nome\":\"Aluguel\"}"));

        lancar(null, null, "10", hoje(), 1).andExpect(status().isBadRequest());
        lancar(cliente, aluguel, "10", hoje(), 1).andExpect(status().isUnprocessableEntity());
        lancar(9999, null, "10", hoje(), 1).andExpect(status().isNotFound());
    }

    @Test
    void cadaRotaSoEnxergaOSeuLado() throws Exception {
        Integer aReceber = primeira(lancar(cliente, null, "10", hoje(), 1));
        Integer aPagar = primeira(enviar(post("/api/v1/contas-a-pagar"), admin,
                "{\"descricao\":\"Aluguel\",\"condicao\":{\"valor\":10,\"primeiroVencimento\":\"%s\"}}".formatted(hoje())));

        mvc.perform(get("/api/v1/contas-a-pagar/" + aReceber).header("Authorization", admin))
                .andExpect(status().isNotFound());
        mvc.perform(get("/api/v1/contas-a-receber/" + aPagar).header("Authorization", admin))
                .andExpect(status().isNotFound());
        receber(aPagar, "PIX", "10").andExpect(status().isNotFound());
        mvc.perform(get("/api/v1/contas-a-receber").header("Authorization", admin))
                .andExpect(jsonPath("$.itens", hasSize(1)));
        mvc.perform(get("/api/v1/contas-a-receber/resumo").header("Authorization", admin))
                .andExpect(jsonPath("$.emAberto.quantidade").value(1));
    }

    @Test
    void inadimplenciaPorClienteEVencimento() throws Exception {
        Integer outroCliente = idDe(enviar(post("/api/v1/clientes"), admin,
                "{\"documento\":\"11444777000161\",\"nome\":\"Mercado Central\"}"));
        lancar(cliente, null, "90", hoje().minusDays(10), 3);
        lancar(outroCliente, null, "40", hoje().minusDays(1), 1);

        mvc.perform(get("/api/v1/contas-a-receber?vencidas=true").header("Authorization", admin))
                .andExpect(jsonPath("$.itens", hasSize(2)))
                .andExpect(jsonPath("$.itens[0].diasEmAtraso").value(10));
        mvc.perform(get("/api/v1/contas-a-receber?clienteId=" + cliente).header("Authorization", admin))
                .andExpect(jsonPath("$.itens", hasSize(3)));
        mvc.perform(get("/api/v1/contas-a-receber/resumo").header("Authorization", admin))
                .andExpect(jsonPath("$.vencidos.quantidade").value(2))
                .andExpect(jsonPath("$.vencidos.valor").value(70))
                .andExpect(jsonPath("$.emAberto.valor").value(130));
    }

    @Test
    void outraEmpresaNaoVeNemRecebeNemUsaClienteAlheio() throws Exception {
        Integer parcela = primeira(lancar(cliente, null, "10", hoje(), 1));

        mvc.perform(get("/api/v1/contas-a-receber/" + parcela).header("Authorization", adminDaB))
                .andExpect(status().isNotFound());
        mvc.perform(get("/api/v1/contas-a-receber").header("Authorization", adminDaB))
                .andExpect(jsonPath("$.itens", hasSize(0)));
        enviar(post("/api/v1/contas-a-receber"), adminDaB, corpo(cliente, null, "10", hoje(), 1))
                .andExpect(status().isNotFound());
        enviar(post("/api/v1/contas-a-receber/%d/recebimentos".formatted(parcela)), adminDaB, baixa("PIX", "10"))
                .andExpect(status().isNotFound());
    }

    @Test
    void quemRecebeNaoPrecisaPoderLancarContas() throws Exception {
        cenario.vincular(cenario.usuario("caixa@a.com"), empresaA,
                cenario.perfil(empresaA, "Recebedor", FINANCEIRO_LER, TITULO_BAIXAR));
        String recebedor = Login.bearer(mvc, "caixa@a.com", empresaA.id());
        Integer parcela = primeira(lancar(cliente, null, "10", hoje(), 1));

        enviar(post("/api/v1/contas-a-receber"), recebedor, corpo(cliente, null, "10", hoje(), 1))
                .andExpect(status().isForbidden());
        enviar(post("/api/v1/contas-a-receber/%d/recebimentos".formatted(parcela)), recebedor, baixa("PIX", "10"))
                .andExpect(status().isOk());
    }

    private ResultActions lancar(Integer clienteId, Integer categoria, String valor, LocalDate vencimento, int parcelas)
            throws Exception {
        return enviar(post("/api/v1/contas-a-receber"), admin, corpo(clienteId, categoria, valor, vencimento, parcelas));
    }

    private static String corpo(Integer clienteId, Integer categoria, String valor, LocalDate vencimento, int parcelas) {
        return """
                {"clienteId":%s,"descricao":"Crediário","categoriaId":%s,
                 "condicao":{"valor":%s,"primeiroVencimento":"%s","parcelas":%d}}"""
                .formatted(clienteId, categoria, valor, vencimento, parcelas);
    }

    private ResultActions receber(Integer titulo, String forma, String valor) throws Exception {
        return enviar(post("/api/v1/contas-a-receber/%d/recebimentos".formatted(titulo)), admin, baixa(forma, valor));
    }

    private String baixa(String forma, String valor) {
        return "{\"contaId\":%d,\"forma\":\"%s\",\"valorPago\":%s}".formatted(caixa, forma, valor);
    }

    private ResultActions estornar(Integer titulo) throws Exception {
        return enviar(post("/api/v1/contas-a-receber/%d/recebimentos/1/estornar".formatted(titulo)), admin,
                "{\"motivo\":\"PIX devolvido\"}");
    }

    private ResultActions titulo(Integer id) throws Exception {
        return mvc.perform(get("/api/v1/contas-a-receber/" + id).header("Authorization", admin));
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
