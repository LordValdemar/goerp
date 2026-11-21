package br.com.goerp.estoque.web;

import static br.com.goerp.usuario.dominio.Permissao.ESTOQUE_LER;
import static br.com.goerp.usuario.dominio.Permissao.ESTOQUE_MOVIMENTAR;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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

class InventarioApiTest extends TesteDeIntegracao {

    @Autowired
    MockMvc mvc;

    @Autowired
    Cenario cenario;

    Empresa empresaA;
    String adminA;
    String adminB;
    Integer refrigerante;
    Integer queijo;
    Integer arroz;
    Integer servico;

    @BeforeEach
    void preparar() throws Exception {
        empresaA = cenario.empresa("11222333000181", "Empresa A LTDA");
        Empresa empresaB = cenario.empresa("12345678000195", "Empresa B LTDA");
        cenario.vincular(cenario.usuario("admin.a@exemplo.com"), empresaA);
        cenario.vincular(cenario.usuario("admin.b@exemplo.com"), empresaB);
        adminA = Login.bearer(mvc, "admin.a@exemplo.com", empresaA.id());
        adminB = Login.bearer(mvc, "admin.b@exemplo.com", empresaB.id());
        refrigerante = produto("REF", "UN", true);
        queijo = produto("QJO", "KG", true);
        arroz = produto("ARZ", "UN", true);
        servico = produto("SRV", "UN", false);
        entrada(refrigerante, "10");
        entrada(queijo, "5");
        entrada(arroz, "20");
    }

    @Test
    void finalizarAjustaCadaProdutoContadoParaAQuantidadeContada() throws Exception {
        Integer inventario = abrir(adminA).andExpect(status().isCreated())
                .andExpect(jsonPath("$.situacao").value("ABERTO"))
                .andReturn().getResponse().getContentAsString().transform(json -> JsonPath.read(json, "$.id"));
        contar(adminA, inventario, refrigerante, "8").andExpect(status().isOk());
        contar(adminA, inventario, queijo, "5.5");

        finalizar(adminA, inventario).andExpect(status().isOk())
                .andExpect(jsonPath("$.situacao").value("FINALIZADO"))
                .andExpect(jsonPath("$.ajustes", hasSize(2)))
                .andExpect(jsonPath("$.ajustes[*].tipo", containsInAnyOrder("AJUSTE_DE_SAIDA", "AJUSTE_DE_ENTRADA")))
                .andExpect(jsonPath("$.ajustes[*].origem", containsInAnyOrder("INVENTARIO", "INVENTARIO")))
                .andExpect(jsonPath("$.ajustes[0].documentoDeOrigem").value(inventario));

        saldo(refrigerante).andExpect(jsonPath("$.quantidade").value(8));
        saldo(queijo).andExpect(jsonPath("$.quantidade").value(5.5));
        saldo(arroz).andExpect(jsonPath("$.quantidade").value(20));
    }

    @Test
    void contagemIgualAoSaldoNaoGeraAjusteERecontagemValeAUltima() throws Exception {
        Integer inventario = idDe(abrir(adminA));
        contar(adminA, inventario, refrigerante, "3");
        contar(adminA, inventario, refrigerante, "10");

        finalizar(adminA, inventario).andExpect(jsonPath("$.ajustes", hasSize(0)));
        saldo(refrigerante).andExpect(jsonPath("$.quantidade").value(10));
    }

    @Test
    void vendaEntreAContagemEAFinalizacaoNaoDistorceOResultado() throws Exception {
        Integer inventario = idDe(abrir(adminA));
        contar(adminA, inventario, refrigerante, "8");
        mvc.perform(post("/api/v1/estoque/saidas").header("Authorization", adminA).contentType(MediaType.APPLICATION_JSON)
                .content("{\"produtoId\":%d,\"quantidade\":1,\"motivo\":\"Venda\"}".formatted(refrigerante)));

        finalizar(adminA, inventario).andExpect(jsonPath("$.ajustes[0].quantidade").value(1));
        saldo(refrigerante).andExpect(jsonPath("$.quantidade").value(8));
    }

    @Test
    void soUmInventarioAbertoPorVezPorEmpresa() throws Exception {
        Integer primeiro = idDe(abrir(adminA));

        abrir(adminA).andExpect(status().isUnprocessableEntity());
        abrir(adminB).andExpect(status().isCreated());

        contar(adminA, primeiro, refrigerante, "10");
        finalizar(adminA, primeiro);
        abrir(adminA).andExpect(status().isCreated());
    }

    @Test
    void cancelarNaoMexeNoEstoqueEEncerradoNaoAceitaMaisNada() throws Exception {
        Integer inventario = idDe(abrir(adminA));
        contar(adminA, inventario, refrigerante, "1");

        mvc.perform(post("/api/v1/estoque/inventarios/" + inventario + "/cancelar").header("Authorization", adminA))
                .andExpect(jsonPath("$.situacao").value("CANCELADO"));

        saldo(refrigerante).andExpect(jsonPath("$.quantidade").value(10));
        contar(adminA, inventario, refrigerante, "2").andExpect(status().isUnprocessableEntity());
        finalizar(adminA, inventario).andExpect(status().isUnprocessableEntity());
    }

    @Test
    void regrasDaContagem() throws Exception {
        Integer inventario = idDe(abrir(adminA));

        finalizar(adminA, inventario).andExpect(status().isUnprocessableEntity());
        contar(adminA, inventario, refrigerante, "1.5").andExpect(status().isUnprocessableEntity());
        contar(adminA, inventario, servico, "1").andExpect(status().isUnprocessableEntity());
        contar(adminA, inventario, refrigerante, "-1").andExpect(status().isUnprocessableEntity());
        contar(adminA, inventario, queijo, "0").andExpect(status().isOk());
    }

    @Test
    void empresaBNaoVeNemMexeNoInventarioDaA() throws Exception {
        Integer inventario = idDe(abrir(adminA));

        mvc.perform(get("/api/v1/estoque/inventarios/" + inventario).header("Authorization", adminB))
                .andExpect(status().isNotFound());
        mvc.perform(get("/api/v1/estoque/inventarios").header("Authorization", adminB)).andExpect(jsonPath("$", hasSize(0)));
        contar(adminB, inventario, refrigerante, "0").andExpect(status().isNotFound());
        finalizar(adminB, inventario).andExpect(status().isNotFound());
    }

    @Test
    void quemContaNaoPrecisaPoderAjustarMasNaoFinaliza() throws Exception {
        Integer inventario = idDe(abrir(adminA));
        cenario.vincular(cenario.usuario("conferente@exemplo.com"), empresaA,
                cenario.perfil(empresaA, "Conferente", ESTOQUE_LER, ESTOQUE_MOVIMENTAR));
        String conferente = Login.bearer(mvc, "conferente@exemplo.com", empresaA.id());

        contar(conferente, inventario, refrigerante, "9").andExpect(status().isOk());
        finalizar(conferente, inventario).andExpect(status().isForbidden());
        abrir(conferente).andExpect(status().isForbidden());
    }

    private ResultActions abrir(String token) throws Exception {
        return mvc.perform(post("/api/v1/estoque/inventarios").header("Authorization", token)
                .contentType(MediaType.APPLICATION_JSON).content("{\"descricao\":\"Inventário de novembro\"}"));
    }

    private ResultActions contar(String token, Integer inventario, Integer produto, String quantidade) throws Exception {
        return mvc.perform(put("/api/v1/estoque/inventarios/%d/contagens/%d".formatted(inventario, produto))
                .header("Authorization", token).contentType(MediaType.APPLICATION_JSON)
                .content("{\"quantidade\":" + quantidade + "}"));
    }

    private ResultActions finalizar(String token, Integer inventario) throws Exception {
        return mvc.perform(post("/api/v1/estoque/inventarios/" + inventario + "/finalizar").header("Authorization", token));
    }

    private ResultActions saldo(Integer produto) throws Exception {
        return mvc.perform(get("/api/v1/estoque/saldos/" + produto).header("Authorization", adminA));
    }

    private void entrada(Integer produto, String quantidade) throws Exception {
        mvc.perform(post("/api/v1/estoque/entradas").header("Authorization", adminA).contentType(MediaType.APPLICATION_JSON)
                .content("{\"produtoId\":%d,\"quantidade\":%s,\"custoUnitario\":5}".formatted(produto, quantidade)))
                .andExpect(status().isCreated());
    }

    private static Integer idDe(ResultActions resposta) throws Exception {
        return JsonPath.read(resposta.andReturn().getResponse().getContentAsString(), "$.id");
    }

    private Integer produto(String codigo, String sigla, boolean controlaEstoque) throws Exception {
        Long unidade = jdbc.queryForObject("SELECT id FROM unidade_medida WHERE empresa_id = ? AND sigla = ?", Long.class,
                empresaA.id(), sigla);
        return idDe(mvc.perform(post("/api/v1/produtos").header("Authorization", adminA)
                .contentType(MediaType.APPLICATION_JSON).content("""
                        {"codigo":"%s","descricao":"Produto %s","unidadeId":%d,"precoDeVenda":10,"ncm":"22021000",
                         "controlaEstoque":%s}""".formatted(codigo, codigo, unidade, controlaEstoque))));
    }
}
