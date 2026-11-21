package br.com.goerp.compras.web;

import static br.com.goerp.usuario.dominio.Permissao.COMPRA_GERENCIAR;
import static br.com.goerp.usuario.dominio.Permissao.COMPRA_LER;
import static org.assertj.core.api.Assertions.assertThat;
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

class RecebimentoApiTest extends TesteDeIntegracao {

    @Autowired
    MockMvc mvc;

    @Autowired
    Cenario cenario;

    Empresa empresaA;
    String admin;
    String adminDaB;
    Integer arroz;
    Integer feijao;
    Integer pedido;
    Integer fornecedor;

    @BeforeEach
    void preparar() throws Exception {
        empresaA = cenario.empresa("11222333000181", "Mercado A LTDA");
        Empresa empresaB = cenario.empresa("12345678000195", "Mercado B LTDA");
        cenario.vincular(cenario.usuario("admin@a.com"), empresaA);
        cenario.vincular(cenario.usuario("admin@b.com"), empresaB);
        admin = Login.bearer(mvc, "admin@a.com", empresaA.id());
        adminDaB = Login.bearer(mvc, "admin@b.com", empresaB.id());
        arroz = produto("ARZ", "18.50");
        feijao = produto("FEJ", "7.25");
        enviar(post("/api/v1/estoque/entradas"), admin,
                "{\"produtoId\":%d,\"quantidade\":10,\"custoUnitario\":16}".formatted(arroz))
                .andExpect(status().isCreated());
        fornecedor = idDe(enviar(post("/api/v1/fornecedores"), admin,
                "{\"documento\":\"11444777000161\",\"nome\":\"Atacado Bom Preço LTDA\"}"));
        pedido = idDe(enviar(post("/api/v1/pedidos-compra"), admin, """
                {"fornecedorId":%d,"itens":[{"produtoId":%d,"quantidade":100},{"produtoId":%d,"quantidade":50}]}"""
                .formatted(fornecedor, arroz, feijao)));
        mvc.perform(post("/api/v1/pedidos-compra/%d/enviar".formatted(pedido)).header("Authorization", admin))
                .andExpect(status().isOk());
    }

    @Test
    void recebimentoParcialDaEntradaNoEstoqueRecalculaCustoMedioEAtualizaOCadastro() throws Exception {
        Integer recebimento = idDe(receber(admin, "1001", "[{\"produtoId\":%d,\"quantidade\":60,\"custoUnitario\":19}]"
                .formatted(arroz)).andExpect(status().isCreated())
                .andExpect(jsonPath("$.valorTotal").value(1140.0)));

        saldo(arroz).andExpect(jsonPath("$.quantidade").value(70))
                // (10 x 16 + 60 x 19) / 70 = 18,5714
                .andExpect(jsonPath("$.custoMedio").value(18.5714));
        pedidoDeCompra().andExpect(jsonPath("$.situacao").value("PARCIALMENTE_RECEBIDO"))
                .andExpect(jsonPath("$.itens[0].pendente").value(40));
        mvc.perform(get("/api/v1/produtos/" + arroz).header("Authorization", admin))
                .andExpect(jsonPath("$.precoDeCusto").value(19.0));
        mvc.perform(get("/api/v1/estoque/produtos/" + arroz + "/extrato").header("Authorization", admin))
                .andExpect(jsonPath("$.itens[0].origem").value("COMPRA"))
                .andExpect(jsonPath("$.itens[0].documentoDeOrigem").value(recebimento));
    }

    @Test
    void completarOsItensDeixaOPedidoRecebidoComOCustoNegociado() throws Exception {
        receber(admin, "NF 1", "[{\"produtoId\":%d,\"quantidade\":60}]".formatted(arroz));
        receber(admin, "NF 2", "[{\"produtoId\":%d,\"quantidade\":40},{\"produtoId\":%d,\"quantidade\":50}]"
                .formatted(arroz, feijao)).andExpect(jsonPath("$.itens[1].custoUnitario").value(7.25));

        pedidoDeCompra().andExpect(jsonPath("$.situacao").value("RECEBIDO"));
        saldo(feijao).andExpect(jsonPath("$.quantidade").value(50));
        mvc.perform(get("/api/v1/pedidos-compra/%d/recebimentos".formatted(pedido)).header("Authorization", admin))
                .andExpect(jsonPath("$", hasSize(2)));
    }

    @Test
    void acimaDoPendenteNadaEhGravado() throws Exception {
        receber(admin, "NF 1", "[{\"produtoId\":%d,\"quantidade\":10},{\"produtoId\":%d,\"quantidade\":101}]"
                .formatted(feijao, arroz))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.detail").value("O produto ARZ tem só 100 a receber; ajuste o pedido antes de receber mais"));

        saldo(feijao).andExpect(jsonPath("$.quantidade").value(0));
        assertThat(jdbc.queryForObject("SELECT count(*) FROM recebimento", Integer.class)).isZero();
        pedidoDeCompra().andExpect(jsonPath("$.situacao").value("ENVIADO"));
    }

    @Test
    void estornoDevolveOPendenteETiraDoEstoqueUmaVezSo() throws Exception {
        Integer recebimento = idDe(receber(admin, "NF 1", "[{\"produtoId\":%d,\"quantidade\":60}]".formatted(arroz)));

        estornar(admin, recebimento).andExpect(status().isOk()).andExpect(jsonPath("$.estornado").value(true));

        saldo(arroz).andExpect(jsonPath("$.quantidade").value(10));
        pedidoDeCompra().andExpect(jsonPath("$.situacao").value("ENVIADO"))
                .andExpect(jsonPath("$.itens[0].pendente").value(100));
        estornar(admin, recebimento).andExpect(status().isUnprocessableEntity());
    }

    @Test
    void naoEstornaRecebimentoDeMercadoriaQueJaSaiu() throws Exception {
        Integer recebimento = idDe(receber(admin, "NF 1", "[{\"produtoId\":%d,\"quantidade\":60}]".formatted(arroz)));
        enviar(post("/api/v1/estoque/saidas"), admin, "{\"produtoId\":%d,\"quantidade\":65,\"motivo\":\"Venda\"}"
                .formatted(arroz)).andExpect(status().isCreated());

        estornar(admin, recebimento).andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.detail").value("Estoque insuficiente: disponível 5, pedido 60"));

        mvc.perform(get("/api/v1/recebimentos/" + recebimento).header("Authorization", admin))
                .andExpect(jsonPath("$.estornado").value(false));
        pedidoDeCompra().andExpect(jsonPath("$.itens[0].recebida").value(60));
    }

    @Test
    void exigeNotaFiscalEProdutoDoPedido() throws Exception {
        receber(admin, "", "[{\"produtoId\":%d,\"quantidade\":1}]".formatted(arroz)).andExpect(status().isBadRequest());
        receber(admin, "NF 1", "[{\"produtoId\":%d,\"quantidade\":1}]".formatted(999))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    void permissaoPropriaParaReceberEIsolamento() throws Exception {
        cenario.vincular(cenario.usuario("comprador@a.com"), empresaA,
                cenario.perfil(empresaA, "Comprador", COMPRA_LER, COMPRA_GERENCIAR));
        String comprador = Login.bearer(mvc, "comprador@a.com", empresaA.id());
        Integer recebimento = idDe(receber(admin, "NF 1", "[{\"produtoId\":%d,\"quantidade\":1}]".formatted(arroz)));

        receber(comprador, "NF 2", "[{\"produtoId\":%d,\"quantidade\":1}]".formatted(arroz))
                .andExpect(status().isForbidden());
        receber(adminDaB, "NF 2", "[{\"produtoId\":%d,\"quantidade\":1}]".formatted(arroz))
                .andExpect(status().isNotFound());
        mvc.perform(get("/api/v1/recebimentos/" + recebimento).header("Authorization", adminDaB))
                .andExpect(status().isNotFound());
        estornar(adminDaB, recebimento).andExpect(status().isNotFound());
    }

    @Test
    void semCondicaoGeraUmaContaAPagarDoFornecedorVencendoNoDia() throws Exception {
        String resposta = receber(admin, "1001", "[{\"produtoId\":%d,\"quantidade\":60,\"custoUnitario\":19}]"
                .formatted(arroz)).andExpect(status().isCreated())
                .andExpect(jsonPath("$.contasAPagar", hasSize(1)))
                .andReturn().getResponse().getContentAsString();
        Integer recebimento = JsonPath.read(resposta, "$.id");
        Integer conta = JsonPath.read(resposta, "$.contasAPagar[0]");

        contaAPagar(conta).andExpect(jsonPath("$.fornecedorId").value(fornecedor))
                .andExpect(jsonPath("$.valor").value(1140))
                .andExpect(jsonPath("$.vencimento").value(hoje().toString()))
                .andExpect(jsonPath("$.descricao").value("Compra NF 1001 (pedido nº 1)"))
                .andExpect(jsonPath("$.documento").value("1001"))
                .andExpect(jsonPath("$.origem").value("RECEBIMENTO_DE_COMPRA"))
                .andExpect(jsonPath("$.documentoDeOrigem").value(recebimento));
        mvc.perform(get("/api/v1/recebimentos/" + recebimento).header("Authorization", admin))
                .andExpect(jsonPath("$.contasAPagar").doesNotExist());
    }

    @Test
    void condicaoParceladaComBoletoECategoria() throws Exception {
        Integer mercadorias = idDe(enviar(post("/api/v1/categorias-financeiras"), admin,
                "{\"tipo\":\"DESPESA\",\"nome\":\"Mercadorias\"}"));

        String resposta = receberCom("NF 2002", "[{\"produtoId\":%d,\"quantidade\":100}]".formatted(arroz),
                "{\"parcelas\":3,\"primeiroVencimento\":\"%s\",\"formaPrevista\":\"BOLETO\",\"categoriaId\":%d}"
                        .formatted(hoje().plusDays(28), mercadorias)).andExpect(status().isCreated())
                .andExpect(jsonPath("$.contasAPagar", hasSize(3)))
                .andReturn().getResponse().getContentAsString();

        // 100 x 18,50 = 1850,00 em 3: 616,68 + 616,66 + 616,66
        contaAPagar(JsonPath.read(resposta, "$.contasAPagar[0]")).andExpect(jsonPath("$.valor").value(616.68))
                .andExpect(jsonPath("$.formaPrevista").value("BOLETO"))
                .andExpect(jsonPath("$.categoriaId").value(mercadorias));
        contaAPagar(JsonPath.read(resposta, "$.contasAPagar[2]")).andExpect(jsonPath("$.parcela").value(3))
                .andExpect(jsonPath("$.vencimento").value(hoje().plusDays(28).plusMonths(2).toString()));
    }

    @Test
    void condicaoInvalidaNaoGravaNemOEstoque() throws Exception {
        Integer receita = idDe(enviar(post("/api/v1/categorias-financeiras"), admin,
                "{\"tipo\":\"RECEITA\",\"nome\":\"Vendas\"}"));
        String itens = "[{\"produtoId\":%d,\"quantidade\":10}]".formatted(arroz);

        receberCom("NF 1", itens, "{\"parcelas\":61}").andExpect(status().isUnprocessableEntity());
        receberCom("NF 1", itens, "{\"categoriaId\":%d}".formatted(receita)).andExpect(status().isUnprocessableEntity());

        saldo(arroz).andExpect(jsonPath("$.quantidade").value(10));
        pedidoDeCompra().andExpect(jsonPath("$.situacao").value("ENVIADO"));
        assertThat(jdbc.queryForObject("SELECT count(*) FROM recebimento", Integer.class)).isZero();
        assertThat(jdbc.queryForObject("SELECT count(*) FROM titulo", Integer.class)).isZero();
    }

    @Test
    void estornoDoRecebimentoCancelaAsContasEERecusadoSeAlgumaJaFoiPaga() throws Exception {
        String resposta = receberCom("1", "[{\"produtoId\":%d,\"quantidade\":60}]".formatted(arroz),
                "{\"parcelas\":2}").andReturn().getResponse().getContentAsString();
        Integer recebimento = JsonPath.read(resposta, "$.id");
        Integer primeira = JsonPath.read(resposta, "$.contasAPagar[0]");
        Integer segunda = JsonPath.read(resposta, "$.contasAPagar[1]");
        Integer banco = idDe(enviar(post("/api/v1/contas-financeiras"), admin,
                "{\"tipo\":\"BANCO\",\"nome\":\"Banco\",\"permiteSaldoNegativo\":true}"));
        enviar(post("/api/v1/contas-a-pagar/%d/pagamentos".formatted(primeira)), admin,
                "{\"contaId\":%d,\"forma\":\"BOLETO\",\"valorPago\":555}".formatted(banco))
                .andExpect(status().isOk());

        estornar(admin, recebimento).andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.detail").value("O título já tem pagamento registrado; estorne os pagamentos antes"));
        saldo(arroz).andExpect(jsonPath("$.quantidade").value(70));
        contaAPagar(segunda).andExpect(jsonPath("$.situacao").value("ABERTO"));

        enviar(post("/api/v1/contas-a-pagar/%d/pagamentos/1/estornar".formatted(primeira)), admin,
                "{\"motivo\":\"Nota devolvida\"}").andExpect(status().isOk());
        estornar(admin, recebimento).andExpect(status().isOk());
        contaAPagar(primeira).andExpect(jsonPath("$.situacao").value("CANCELADO"))
                .andExpect(jsonPath("$.motivoDoCancelamento").value(
                        "Recebimento da NF 1 estornado: Nota lançada no pedido errado"));
        contaAPagar(segunda).andExpect(jsonPath("$.situacao").value("CANCELADO"));
        saldo(arroz).andExpect(jsonPath("$.quantidade").value(10));
    }

    private ResultActions receberCom(String nota, String itens, String pagamento) throws Exception {
        return enviar(post("/api/v1/pedidos-compra/%d/recebimentos".formatted(pedido)), admin,
                "{\"notaFiscal\":\"%s\",\"itens\":%s,\"pagamento\":%s}".formatted(nota, itens, pagamento));
    }

    private ResultActions contaAPagar(Integer id) throws Exception {
        return mvc.perform(get("/api/v1/contas-a-pagar/" + id).header("Authorization", admin));
    }

    private LocalDate hoje() {
        return LocalDate.ofInstant(relogio.instant(), ZoneId.of("America/Sao_Paulo"));
    }

    private ResultActions receber(String token, String nota, String itens) throws Exception {
        return enviar(post("/api/v1/pedidos-compra/%d/recebimentos".formatted(pedido)), token,
                "{\"notaFiscal\":\"%s\",\"itens\":%s}".formatted(nota, itens));
    }

    private ResultActions estornar(String token, Integer recebimento) throws Exception {
        return enviar(post("/api/v1/recebimentos/%d/estornar".formatted(recebimento)), token,
                "{\"motivo\":\"Nota lançada no pedido errado\"}");
    }

    private ResultActions pedidoDeCompra() throws Exception {
        return mvc.perform(get("/api/v1/pedidos-compra/" + pedido).header("Authorization", admin));
    }

    private ResultActions saldo(Integer produto) throws Exception {
        return mvc.perform(get("/api/v1/estoque/saldos/" + produto).header("Authorization", admin));
    }

    private ResultActions enviar(MockHttpServletRequestBuilder requisicao, String token, String corpo) throws Exception {
        return mvc.perform(requisicao.header("Authorization", token).contentType(MediaType.APPLICATION_JSON).content(corpo));
    }

    private Integer produto(String codigo, String custo) throws Exception {
        Long un = jdbc.queryForObject("SELECT id FROM unidade_medida WHERE empresa_id = ? AND sigla = 'UN'", Long.class,
                empresaA.id());
        return idDe(enviar(post("/api/v1/produtos"), admin, """
                {"codigo":"%s","descricao":"Produto %s","unidadeId":%d,"precoDeVenda":30,"precoDeCusto":%s,
                 "ncm":"10063021","controlaEstoque":true}""".formatted(codigo, codigo, un, custo)));
    }

    private static Integer idDe(ResultActions resposta) throws Exception {
        return JsonPath.read(resposta.andReturn().getResponse().getContentAsString(), "$.id");
    }
}
