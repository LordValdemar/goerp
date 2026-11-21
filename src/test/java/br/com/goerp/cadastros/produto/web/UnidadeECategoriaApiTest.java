package br.com.goerp.cadastros.produto.web;

import static br.com.goerp.usuario.dominio.Permissao.PRODUTO_GERENCIAR;
import static br.com.goerp.usuario.dominio.Permissao.PRODUTO_LER;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;
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

import br.com.goerp.empresa.dominio.Empresa;
import br.com.goerp.suporte.Cenario;
import br.com.goerp.suporte.Login;
import br.com.goerp.suporte.TesteDeIntegracao;
import br.com.goerp.usuario.dominio.Perfil;

class UnidadeECategoriaApiTest extends TesteDeIntegracao {

    @Autowired
    MockMvc mvc;

    @Autowired
    Cenario cenario;

    Empresa empresaA;
    Empresa empresaB;
    String produtosA;
    String produtosB;

    @BeforeEach
    void preparar() throws Exception {
        empresaA = cenario.empresa("11222333000181", "Empresa A LTDA");
        empresaB = cenario.empresa("12345678000195", "Empresa B LTDA");
        produtosA = entrar("a@exemplo.com", empresaA, cenario.perfil(empresaA, "Produtos", PRODUTO_LER, PRODUTO_GERENCIAR));
        produtosB = entrar("b@exemplo.com", empresaB, cenario.perfil(empresaB, "Produtos", PRODUTO_LER, PRODUTO_GERENCIAR));
    }

    @Test
    void empresaJaComecaComAsUnidadesPadrao() throws Exception {
        mvc.perform(get("/api/v1/unidades").header("Authorization", produtosA))
                .andExpect(jsonPath("$", hasSize(10)))
                .andExpect(jsonPath("$[?(@.sigla == 'KG')].fracionavel", contains(true)))
                .andExpect(jsonPath("$[?(@.sigla == 'UN')].fracionavel", contains(false)));
    }

    @Test
    void cadastraUnidadeNovaComSiglaNormalizadaENaoRepete() throws Exception {
        criar("/api/v1/unidades", produtosA, "{\"sigla\":\"fd\",\"descricao\":\"Fardo\",\"fracionavel\":false}")
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.sigla").value("FD"));

        criar("/api/v1/unidades", produtosA, "{\"sigla\":\"FD\",\"descricao\":\"Outro\"}")
                .andExpect(status().isUnprocessableEntity());
        criar("/api/v1/unidades", produtosA, "{\"sigla\":\"CAIXA12\",\"descricao\":\"Longa\"}")
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    void alterarUnidadeNaoMudaASigla() throws Exception {
        String local = criar("/api/v1/unidades", produtosA, "{\"sigla\":\"FD\",\"descricao\":\"Fardo\"}")
                .andReturn().getResponse().getHeader("Location");

        mvc.perform(put(local).header("Authorization", produtosA).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"sigla\":\"XX\",\"descricao\":\"Fardo 12\",\"fracionavel\":true}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sigla").value("FD"))
                .andExpect(jsonPath("$.descricao").value("Fardo 12"));
    }

    @Test
    void inativaEFiltraUnidades() throws Exception {
        String local = criar("/api/v1/unidades", produtosA, "{\"sigla\":\"FD\",\"descricao\":\"Fardo\"}")
                .andReturn().getResponse().getHeader("Location");

        mvc.perform(delete(local).header("Authorization", produtosA)).andExpect(status().isNoContent());

        mvc.perform(get("/api/v1/unidades?ativa=true").header("Authorization", produtosA))
                .andExpect(jsonPath("$[*].sigla", not(hasItem("FD"))));
        mvc.perform(get("/api/v1/unidades?ativa=false").header("Authorization", produtosA))
                .andExpect(jsonPath("$[*].sigla", contains("FD")));
        mvc.perform(post(local + "/reativar").header("Authorization", produtosA)).andExpect(jsonPath("$.ativa").value(true));
    }

    @Test
    void categoriasComNomeUnicoPorEmpresa() throws Exception {
        criar("/api/v1/categorias", produtosA, "{\"nome\":\"Bebidas\"}").andExpect(status().isCreated());
        String limpeza = criar("/api/v1/categorias", produtosA, "{\"nome\":\"Limpeza\"}")
                .andReturn().getResponse().getHeader("Location");

        criar("/api/v1/categorias", produtosA, "{\"nome\":\"BEBIDAS\"}").andExpect(status().isUnprocessableEntity());
        criar("/api/v1/categorias", produtosB, "{\"nome\":\"Bebidas\"}").andExpect(status().isCreated());
        mvc.perform(put(limpeza).header("Authorization", produtosA).contentType(MediaType.APPLICATION_JSON)
                .content("{\"nome\":\"bebidas\"}")).andExpect(status().isUnprocessableEntity());

        mvc.perform(get("/api/v1/categorias").header("Authorization", produtosA))
                .andExpect(jsonPath("$[*].nome", contains("Bebidas", "Limpeza")));
    }

    @Test
    void empresaANaoVeNemAlteraUnidadesECategoriasDaB() throws Exception {
        String unidadeDaB = criar("/api/v1/unidades", produtosB, "{\"sigla\":\"FD\",\"descricao\":\"Fardo\"}")
                .andReturn().getResponse().getHeader("Location");
        String categoriaDaB = criar("/api/v1/categorias", produtosB, "{\"nome\":\"Só da B\"}")
                .andReturn().getResponse().getHeader("Location");

        mvc.perform(get("/api/v1/unidades").header("Authorization", produtosA)).andExpect(jsonPath("$", hasSize(10)));
        mvc.perform(get("/api/v1/categorias").header("Authorization", produtosA)).andExpect(jsonPath("$", hasSize(0)));
        mvc.perform(get(unidadeDaB).header("Authorization", produtosA)).andExpect(status().isNotFound());
        mvc.perform(delete(categoriaDaB).header("Authorization", produtosA)).andExpect(status().isNotFound());
        mvc.perform(put(categoriaDaB).header("Authorization", produtosA).contentType(MediaType.APPLICATION_JSON)
                .content("{\"nome\":\"Invadida\"}")).andExpect(status().isNotFound());
    }

    @Test
    void permissoes() throws Exception {
        String soLeitura = entrar("leitor@exemplo.com", empresaA, cenario.perfil(empresaA, "Leitura", PRODUTO_LER));

        mvc.perform(get("/api/v1/unidades").header("Authorization", soLeitura)).andExpect(status().isOk());
        criar("/api/v1/categorias", soLeitura, "{\"nome\":\"X\"}").andExpect(status().isForbidden());
    }

    private ResultActions criar(String rota, String token, String corpo) throws Exception {
        return mvc.perform(post(rota).header("Authorization", token).contentType(MediaType.APPLICATION_JSON).content(corpo));
    }

    private String entrar(String email, Empresa empresa, Perfil perfil) throws Exception {
        cenario.vincular(cenario.usuario(email), empresa, perfil);
        return Login.bearer(mvc, email, empresa.id());
    }
}
