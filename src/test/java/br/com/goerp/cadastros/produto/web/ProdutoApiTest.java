package br.com.goerp.cadastros.produto.web;

import static br.com.goerp.usuario.dominio.Permissao.PRODUTO_GERENCIAR;
import static br.com.goerp.usuario.dominio.Permissao.PRODUTO_LER;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
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
import br.com.goerp.usuario.dominio.Perfil;

class ProdutoApiTest extends TesteDeIntegracao {

    @Autowired
    MockMvc mvc;

    @Autowired
    Cenario cenario;

    Empresa empresaA;
    Empresa empresaB;
    String tokenA;
    String tokenB;
    Long unidadeA;
    Long quiloA;
    Long unidadeB;
    Integer bebidasA;

    @BeforeEach
    void preparar() throws Exception {
        empresaA = cenario.empresa("11222333000181", "Empresa A LTDA");
        empresaB = cenario.empresa("12345678000195", "Empresa B LTDA");
        tokenA = entrar("a@exemplo.com", empresaA, cenario.perfil(empresaA, "Produtos", PRODUTO_LER, PRODUTO_GERENCIAR));
        tokenB = entrar("b@exemplo.com", empresaB, cenario.perfil(empresaB, "Produtos", PRODUTO_LER, PRODUTO_GERENCIAR));
        unidadeA = unidade(empresaA, "UN");
        quiloA = unidade(empresaA, "KG");
        unidadeB = unidade(empresaB, "UN");
        bebidasA = JsonPath.read(criar("/api/v1/categorias", tokenA, "{\"nome\":\"Bebidas\"}")
                .andReturn().getResponse().getContentAsString(), "$.id");
    }

    @Test
    void cadastraProdutoCompleto() throws Exception {
        String local = criar("/api/v1/produtos", tokenA, refrigerante("ref-001", "7891000315507", unidadeA))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.codigo").value("REF-001"))
                .andExpect(jsonPath("$.unidade.sigla").value("UN"))
                .andExpect(jsonPath("$.categoria.nome").value("Bebidas"))
                .andExpect(jsonPath("$.precoDeVenda").value(8.99))
                .andExpect(jsonPath("$.precoDeCusto").value(5.3333))
                .andExpect(jsonPath("$.ncm").value("22021000"))
                .andExpect(jsonPath("$.origem").value("NACIONAL"))
                .andReturn().getResponse().getHeader("Location");

        mvc.perform(get(local).header("Authorization", tokenA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.codigoDeBarras").value("7891000315507"));
    }

    @Test
    void codigoECodigoDeBarrasUnicosNaEmpresaMasLivresEntreEmpresas() throws Exception {
        criar("/api/v1/produtos", tokenA, refrigerante("REF-001", "7891000315507", unidadeA)).andExpect(status().isCreated());

        criar("/api/v1/produtos", tokenA, refrigerante("ref-001", null, unidadeA)).andExpect(status().isUnprocessableEntity());
        criar("/api/v1/produtos", tokenA, refrigerante("REF-002", "7891000315507", unidadeA))
                .andExpect(status().isUnprocessableEntity());
        criar("/api/v1/produtos", tokenB, refrigerante("REF-001", "7891000315507", unidadeB)
                .replace(",\"categoriaId\":" + bebidasA, "")).andExpect(status().isCreated());
    }

    @Test
    void regrasDeNegocio() throws Exception {
        criar("/api/v1/produtos", tokenA, refrigerante("A1", "7891000315508", unidadeA))
                .andExpect(status().isUnprocessableEntity());
        criar("/api/v1/produtos", tokenA, refrigerante("A2", null, unidadeA).replace("\"estoqueMinimo\":6",
                "\"estoqueMinimo\":2.5")).andExpect(status().isUnprocessableEntity());
        criar("/api/v1/produtos", tokenA, refrigerante("A3", null, quiloA).replace("\"estoqueMinimo\":6",
                "\"estoqueMinimo\":2.5")).andExpect(status().isCreated());
        criar("/api/v1/produtos", tokenA, refrigerante("A4", null, unidadeA).replace("8.99", "8.999"))
                .andExpect(status().isUnprocessableEntity());
        criar("/api/v1/produtos", tokenA, refrigerante("A5", null, unidadeA).replace("2202.10.00", "2202"))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    void naoUsaUnidadeOuCategoriaDeOutraEmpresa() throws Exception {
        // unidade da B usada pela A
        criar("/api/v1/produtos", tokenA, refrigerante("A1", null, unidadeB)).andExpect(status().isNotFound());
        // categoria da A usada pela B
        criar("/api/v1/produtos", tokenB, refrigerante("B1", null, unidadeB)).andExpect(status().isNotFound());
    }

    @Test
    void naoUsaCategoriaInativa() throws Exception {
        mvc.perform(delete("/api/v1/categorias/" + bebidasA).header("Authorization", tokenA));

        criar("/api/v1/produtos", tokenA, refrigerante("A1", null, unidadeA)).andExpect(status().isUnprocessableEntity());
    }

    @Test
    void pesquisaPorDescricaoCodigoCodigoDeBarrasECategoria() throws Exception {
        criar("/api/v1/produtos", tokenA, refrigerante("REF-001", "7891000315507", unidadeA));
        criar("/api/v1/produtos", tokenA, """
                {"codigo":"ARZ-5","descricao":"Arroz tipo 1 5kg","unidadeId":%d,"precoDeVenda":24.90,
                 "ncm":"10063021","controlaEstoque":true}""".formatted(unidadeA));

        pesquisar("?busca=arroz").andExpect(jsonPath("$.itens[*].codigo", contains("ARZ-5")));
        pesquisar("?busca=ref-0").andExpect(jsonPath("$.itens[*].codigo", contains("REF-001")));
        pesquisar("?busca=789100").andExpect(jsonPath("$.itens[*].codigo", contains("REF-001")));
        pesquisar("?categoriaId=" + bebidasA).andExpect(jsonPath("$.itens[*].codigo", contains("REF-001")));
        pesquisar("").andExpect(jsonPath("$.itens[*].codigo", contains("ARZ-5", "REF-001")));
    }

    @Test
    void empresaBNaoVeProdutosDaA() throws Exception {
        String local = criar("/api/v1/produtos", tokenA, refrigerante("REF-001", null, unidadeA))
                .andReturn().getResponse().getHeader("Location");

        mvc.perform(get("/api/v1/produtos").header("Authorization", tokenB)).andExpect(jsonPath("$.total").value(0));
        mvc.perform(get(local).header("Authorization", tokenB)).andExpect(status().isNotFound());
    }

    @Test
    void soQuemGerenciaCadastra() throws Exception {
        String leitor = entrar("leitor@exemplo.com", empresaA, cenario.perfil(empresaA, "Leitura", PRODUTO_LER));

        criar("/api/v1/produtos", leitor, refrigerante("A1", null, unidadeA)).andExpect(status().isForbidden());
        mvc.perform(get("/api/v1/produtos").header("Authorization", leitor)).andExpect(status().isOk());
    }

    @Test
    void alteraComAVersaoLidaEDetectaEdicaoSimultanea() throws Exception {
        String local = criarRefrigerante();

        alterar(local, alteracao(0, "9.49")).andExpect(status().isOk())
                .andExpect(jsonPath("$.precoDeVenda").value(9.49))
                .andExpect(jsonPath("$.codigo").value("REF-001"))
                .andExpect(jsonPath("$.versao").value(1));

        alterar(local, alteracao(0, "7.00")).andExpect(status().isConflict());
        mvc.perform(get(local).header("Authorization", tokenA)).andExpect(jsonPath("$.precoDeVenda").value(9.49));
    }

    @Test
    void historicoGuardaCadaPrecoComQuemAlterouEQuando() throws Exception {
        String local = criarRefrigerante();
        alterar(local, alteracao(0, "9.49"));
        alterar(local, alteracao(1, "9.99"));
        Long id = Long.valueOf(local.substring(local.lastIndexOf('/') + 1));

        var historico = jdbc.queryForList("""
                SELECT a.preco_venda, r.usuario_id, r.empresa_id, r.momento
                FROM produto_aud a JOIN revisao r ON r.id = a.rev WHERE a.id = ? ORDER BY a.rev""", id);

        assertThat(historico).extracting(linha -> linha.get("preco_venda").toString())
                .containsExactly("8.99", "9.49", "9.99");
        assertThat(historico).allSatisfy(linha -> {
            assertThat(linha.get("usuario_id")).isNotNull();
            assertThat(linha.get("empresa_id")).isEqualTo(empresaA.id());
            assertThat(linha.get("momento")).isNotNull();
        });
    }

    @Test
    void produtoContinuaEditavelDepoisQueSuaCategoriaFoiInativada() throws Exception {
        String local = criarRefrigerante();
        mvc.perform(delete("/api/v1/categorias/" + bebidasA).header("Authorization", tokenA));

        alterar(local, alteracao(0, "9.49")).andExpect(status().isOk());
    }

    @Test
    void naoTrocaParaCategoriaInativaNemUsaCodigoDeBarrasDeOutroProduto() throws Exception {
        String local = criarRefrigerante();
        criar("/api/v1/produtos", tokenA, refrigerante("REF-002", "96385074", unidadeA));
        Integer antiga = JsonPath.read(criar("/api/v1/categorias", tokenA, "{\"nome\":\"Antiga\"}")
                .andReturn().getResponse().getContentAsString(), "$.id");
        mvc.perform(delete("/api/v1/categorias/" + antiga).header("Authorization", tokenA));

        alterar(local, alteracao(0, "8.99").replace("\"categoriaId\":" + bebidasA, "\"categoriaId\":" + antiga))
                .andExpect(status().isUnprocessableEntity());
        alterar(local, alteracao(0, "8.99").replace("7891000315507", "96385074"))
                .andExpect(status().isUnprocessableEntity());
        alterar(local, alteracao(0, "8.99")).andExpect(status().isOk());
    }

    @Test
    void buscaPorCodigoDeBarrasSoEncontraProdutoAtivo() throws Exception {
        String local = criarRefrigerante();

        mvc.perform(get("/api/v1/produtos/codigo-barras/7891000315507").header("Authorization", tokenA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.codigo").value("REF-001"))
                .andExpect(jsonPath("$.unidade.sigla").value("UN"));

        mvc.perform(delete(local).header("Authorization", tokenA)).andExpect(status().isNoContent());
        mvc.perform(get("/api/v1/produtos/codigo-barras/7891000315507").header("Authorization", tokenA))
                .andExpect(status().isNotFound());

        mvc.perform(post(local + "/reativar").header("Authorization", tokenA)).andExpect(jsonPath("$.ativo").value(true));
        mvc.perform(get("/api/v1/produtos/codigo-barras/7891000315507").header("Authorization", tokenA))
                .andExpect(status().isOk());
    }

    @Test
    void codigoDeBarrasInvalidoDa422() throws Exception {
        mvc.perform(get("/api/v1/produtos/codigo-barras/7891000315508").header("Authorization", tokenA))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    void empresaBNaoAchaNemAlteraProdutoDaA() throws Exception {
        String local = criarRefrigerante();

        mvc.perform(get("/api/v1/produtos/codigo-barras/7891000315507").header("Authorization", tokenB))
                .andExpect(status().isNotFound());
        mvc.perform(put(local).header("Authorization", tokenB).contentType(MediaType.APPLICATION_JSON)
                .content(alteracao(0, "1.00"))).andExpect(status().isNotFound());
        mvc.perform(delete(local).header("Authorization", tokenB)).andExpect(status().isNotFound());
        mvc.perform(get(local).header("Authorization", tokenA)).andExpect(jsonPath("$.ativo").value(true));
    }

    private String criarRefrigerante() throws Exception {
        return criar("/api/v1/produtos", tokenA, refrigerante("REF-001", "7891000315507", unidadeA))
                .andReturn().getResponse().getHeader("Location");
    }

    private String alteracao(int versao, String preco) {
        return refrigerante("REF-001", "7891000315507", unidadeA).replace("8.99", preco)
                .replace("{", "{\"versao\":" + versao + ",");
    }

    private ResultActions alterar(String local, String corpo) throws Exception {
        return mvc.perform(put(local).header("Authorization", tokenA).contentType(MediaType.APPLICATION_JSON).content(corpo));
    }

    /** Produto de exemplo, na categoria Bebidas da empresa A. */
    private String refrigerante(String codigo, String gtin, Long unidadeId) {
        String codigoDeBarras = gtin == null ? "null" : "\"" + gtin + "\"";
        return ("{\"codigo\":\"%s\",\"descricao\":\"Refrigerante Cola 2L\",\"codigoDeBarras\":%s,"
                + "\"unidadeId\":%d,\"categoriaId\":%d,\"precoDeVenda\":8.99,\"precoDeCusto\":5.3333,"
                + "\"ncm\":\"2202.10.00\",\"controlaEstoque\":true,\"estoqueMinimo\":6}")
                .formatted(codigo, codigoDeBarras, unidadeId, bebidasA);
    }

    private ResultActions pesquisar(String parametros) throws Exception {
        return mvc.perform(get("/api/v1/produtos" + parametros).header("Authorization", tokenA));
    }

    private ResultActions criar(String rota, String token, String corpo) throws Exception {
        return mvc.perform(post(rota).header("Authorization", token).contentType(MediaType.APPLICATION_JSON).content(corpo));
    }

    private Long unidade(Empresa empresa, String sigla) {
        return jdbc.queryForObject("SELECT id FROM unidade_medida WHERE empresa_id = ? AND sigla = ?", Long.class,
                empresa.id(), sigla);
    }

    private String entrar(String email, Empresa empresa, Perfil perfil) throws Exception {
        cenario.vincular(cenario.usuario(email), empresa, perfil);
        return Login.bearer(mvc, email, empresa.id());
    }
}
