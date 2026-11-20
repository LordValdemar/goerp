package br.com.goerp.cadastros.produto.web;

import static br.com.goerp.usuario.dominio.Permissao.PRODUTO_GERENCIAR;
import static br.com.goerp.usuario.dominio.Permissao.PRODUTO_LER;
import static org.hamcrest.Matchers.contains;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
