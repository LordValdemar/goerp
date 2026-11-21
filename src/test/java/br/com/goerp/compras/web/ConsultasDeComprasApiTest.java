package br.com.goerp.compras.web;

import static br.com.goerp.usuario.dominio.Permissao.ESTOQUE_LER;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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

class ConsultasDeComprasApiTest extends TesteDeIntegracao {

    @Autowired
    MockMvc mvc;

    @Autowired
    Cenario cenario;

    Empresa empresaA;
    String admin;
    String adminDaB;
    Integer arroz;
    Integer atacado;
    Integer distribuidora;
    Integer pedidoDoAtacado;
    Integer pedidoDaDistribuidora;

    @BeforeEach
    void preparar() throws Exception {
        empresaA = cenario.empresa("11222333000181", "Mercado A LTDA");
        Empresa empresaB = cenario.empresa("12345678000195", "Mercado B LTDA");
        cenario.vincular(cenario.usuario("admin@a.com"), empresaA);
        cenario.vincular(cenario.usuario("admin@b.com"), empresaB);
        admin = Login.bearer(mvc, "admin@a.com", empresaA.id());
        adminDaB = Login.bearer(mvc, "admin@b.com", empresaB.id());
        arroz = produto("ARZ", "18.50");
        Integer feijao = produto("FEJ", "7.25");
        atacado = fornecedor("11444777000161", "Atacado Bom Preço LTDA");
        distribuidora = fornecedor("11222333000262", "Distribuidora Central LTDA");
        pedidoDoAtacado = pedidoEnviado(atacado, 2, "[{\"produtoId\":%d,\"quantidade\":100},{\"produtoId\":%d,\"quantidade\":50}]"
                .formatted(arroz, feijao));
        pedidoDaDistribuidora = pedidoEnviado(distribuidora, 5, "[{\"produtoId\":%d,\"quantidade\":10}]".formatted(arroz));
        enviar(post("/api/v1/pedidos-compra"), admin, "{\"fornecedorId\":%d,\"itens\":[{\"produtoId\":%d,\"quantidade\":1}]}"
                .formatted(atacado, arroz)).andExpect(status().isCreated());
    }

    @Test
    void pendentesMostramOValorQueFaltaChegarSemOsRascunhos() throws Exception {
        receber(pedidoDoAtacado, 60);

        pendentes("").andExpect(status().isOk())
                .andExpect(jsonPath("$.pedidos", hasSize(2)))
                // previsão mais próxima primeiro
                .andExpect(jsonPath("$.pedidos[0].pedido.id").value(pedidoDoAtacado))
                // 40 x 18,50 + 50 x 7,25
                .andExpect(jsonPath("$.pedidos[0].valorPendente").value(1102.50))
                .andExpect(jsonPath("$.pedidos[1].valorPendente").value(185.00))
                .andExpect(jsonPath("$.valorAReceber").value(1287.50))
                .andExpect(jsonPath("$.atrasados").value(0));
        pendentes("?fornecedorId=" + distribuidora).andExpect(jsonPath("$.pedidos", hasSize(1)))
                .andExpect(jsonPath("$.pedidos[0].pedido.id").value(pedidoDaDistribuidora));
    }

    @Test
    void filtraOsAtrasados() throws Exception {
        relogio.avancar(Duration.ofDays(3));

        pendentes("").andExpect(jsonPath("$.atrasados").value(1));
        pendentes("?atrasados=true").andExpect(jsonPath("$.pedidos", hasSize(1)))
                .andExpect(jsonPath("$.pedidos[0].pedido.id").value(pedidoDoAtacado))
                .andExpect(jsonPath("$.pedidos[0].pedido.atrasado").value(true))
                .andExpect(jsonPath("$.valorAReceber").value(2212.50));
    }

    @Test
    void pedidoRecebidoPorCompletoSaiDosPendentes() throws Exception {
        receber(pedidoDaDistribuidora, 10);

        pendentes("").andExpect(jsonPath("$.pedidos", hasSize(1)))
                .andExpect(jsonPath("$.pedidos[0].pedido.id").value(pedidoDoAtacado));
    }

    @Test
    void recebimentosDoPeriodoTotalizamPorFornecedorSemOsEstornados() throws Exception {
        receber(pedidoDoAtacado, 60);
        receber(pedidoDoAtacado, 10);
        Integer estornado = receber(pedidoDaDistribuidora, 10);
        enviar(post("/api/v1/recebimentos/%d/estornar".formatted(estornado)), admin, "{\"motivo\":\"Nota errada\"}")
                .andExpect(status().isOk());
        String hoje = hoje().toString();

        recebimentos("?de=%s&ate=%s".formatted(hoje, hoje)).andExpect(status().isOk())
                .andExpect(jsonPath("$.recebimentos", hasSize(2)))
                .andExpect(jsonPath("$.recebimentos[0].recebimento.valorTotal").value(185.00))
                .andExpect(jsonPath("$.recebimentos[0].fornecedorId").value(atacado))
                .andExpect(jsonPath("$.total").value(1295.00))
                .andExpect(jsonPath("$.totalPorFornecedor['%d']".formatted(atacado)).value(1295.00))
                .andExpect(jsonPath("$.totalPorFornecedor['%d']".formatted(distribuidora)).doesNotExist());
        recebimentos("?de=%s&ate=%s&incluirEstornados=true".formatted(hoje, hoje))
                .andExpect(jsonPath("$.recebimentos", hasSize(3)))
                .andExpect(jsonPath("$.recebimentos[0].recebimento.estornado").value(true))
                .andExpect(jsonPath("$.recebimentos[0].numeroDoPedido").isNumber())
                .andExpect(jsonPath("$.total").value(1295.00));
        recebimentos("?de=%s&ate=%s&fornecedorId=%d&incluirEstornados=true".formatted(hoje, hoje, distribuidora))
                .andExpect(jsonPath("$.recebimentos", hasSize(1)))
                .andExpect(jsonPath("$.total").value(0));
        String amanha = hoje().plusDays(1).toString();
        recebimentos("?de=%s&ate=%s".formatted(amanha, amanha)).andExpect(jsonPath("$.recebimentos", hasSize(0)));
    }

    @Test
    void periodoObrigatorioCoerenteEDeNoMaximoUmAno() throws Exception {
        recebimentos("?de=2026-01-10").andExpect(status().isBadRequest());
        recebimentos("?de=2026-01-10&ate=2026-01-09").andExpect(status().isUnprocessableEntity());
        recebimentos("?de=2025-01-01&ate=2026-01-02").andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.detail").value("O período pode ter no máximo 366 dias"));
        // 366 dias contando o primeiro e o último
        recebimentos("?de=2025-01-01&ate=2026-01-01").andExpect(status().isOk());
    }

    @Test
    void outraEmpresaNaoVeEExigePermissaoDeLeitura() throws Exception {
        receber(pedidoDoAtacado, 60);
        String hoje = hoje().toString();
        cenario.vincular(cenario.usuario("estoquista@a.com"), empresaA, cenario.perfil(empresaA, "Estoquista", ESTOQUE_LER));
        String estoquista = Login.bearer(mvc, "estoquista@a.com", empresaA.id());

        mvc.perform(get("/api/v1/pedidos-compra/pendentes").header("Authorization", adminDaB))
                .andExpect(jsonPath("$.pedidos", hasSize(0))).andExpect(jsonPath("$.valorAReceber").value(0));
        mvc.perform(get("/api/v1/recebimentos?de=%s&ate=%s".formatted(hoje, hoje)).header("Authorization", adminDaB))
                .andExpect(jsonPath("$.recebimentos", hasSize(0))).andExpect(jsonPath("$.total").value(0));
        mvc.perform(get("/api/v1/pedidos-compra/pendentes").header("Authorization", estoquista))
                .andExpect(status().isForbidden());
        mvc.perform(get("/api/v1/recebimentos?de=%s&ate=%s".formatted(hoje, hoje)).header("Authorization", estoquista))
                .andExpect(status().isForbidden());
    }

    private ResultActions pendentes(String filtro) throws Exception {
        return mvc.perform(get("/api/v1/pedidos-compra/pendentes" + filtro).header("Authorization", admin));
    }

    private ResultActions recebimentos(String filtro) throws Exception {
        return mvc.perform(get("/api/v1/recebimentos" + filtro).header("Authorization", admin));
    }

    private Integer receber(Integer pedido, int quantidadeDeArroz) throws Exception {
        return idDe(enviar(post("/api/v1/pedidos-compra/%d/recebimentos".formatted(pedido)), admin,
                "{\"notaFiscal\":\"NF %d\",\"itens\":[{\"produtoId\":%d,\"quantidade\":%d}]}"
                        .formatted(quantidadeDeArroz, arroz, quantidadeDeArroz)).andExpect(status().isCreated()));
    }

    private Integer pedidoEnviado(Integer fornecedor, int diasParaEntrega, String itens) throws Exception {
        Integer pedido = idDe(enviar(post("/api/v1/pedidos-compra"), admin,
                "{\"fornecedorId\":%d,\"itens\":%s,\"previsaoDeEntrega\":\"%s\"}"
                        .formatted(fornecedor, itens, hoje().plusDays(diasParaEntrega))).andExpect(status().isCreated()));
        mvc.perform(post("/api/v1/pedidos-compra/%d/enviar".formatted(pedido)).header("Authorization", admin))
                .andExpect(status().isOk());
        return pedido;
    }

    private Integer fornecedor(String cnpj, String nome) throws Exception {
        return idDe(enviar(post("/api/v1/fornecedores"), admin, "{\"documento\":\"%s\",\"nome\":\"%s\"}".formatted(cnpj, nome))
                .andExpect(status().isCreated()));
    }

    private Integer produto(String codigo, String custo) throws Exception {
        Long un = jdbc.queryForObject("SELECT id FROM unidade_medida WHERE empresa_id = ? AND sigla = 'UN'", Long.class,
                empresaA.id());
        return idDe(enviar(post("/api/v1/produtos"), admin, """
                {"codigo":"%s","descricao":"Produto %s","unidadeId":%d,"precoDeVenda":30,"precoDeCusto":%s,
                 "ncm":"10063021","controlaEstoque":true}""".formatted(codigo, codigo, un, custo)));
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
