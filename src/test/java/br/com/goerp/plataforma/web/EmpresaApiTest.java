package br.com.goerp.plataforma.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
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

import br.com.goerp.suporte.Cenario;
import br.com.goerp.suporte.TesteDeIntegracao;
import br.com.goerp.usuario.aplicacao.CriarAdministradorDaPlataforma;
import br.com.goerp.usuario.dominio.Permissao;

class EmpresaApiTest extends TesteDeIntegracao {

    private static final String NOVA_EMPRESA = """
            {"cnpj":"12.345.678/0001-95","razaoSocial":"Mercado Bom Preço LTDA","nomeFantasia":"Bom Preço",
             "administrador":{"nome":"Dona Rosa","email":"rosa@bompreco.com","senha":"senhaRosa1"}}
            """;

    @Autowired
    MockMvc mvc;

    @Autowired
    Cenario cenario;

    @Autowired
    CriarAdministradorDaPlataforma criarAdministradorDaPlataforma;

    String plataforma;

    @BeforeEach
    void entrarComoAdministradorDaPlataforma() throws Exception {
        criarAdministradorDaPlataforma.executar("Admin", "admin@goerp.local", "senhaAdmin1");
        plataforma = "Bearer " + JsonPath.read(login("admin@goerp.local", "senhaAdmin1", null)
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString(), "$.tokenDeAcesso");
    }

    @Test
    void empresaNasceProntaParaUsoComAdministradorQueTemTodasAsPermissoes() throws Exception {
        String resposta = cadastrar(NOVA_EMPRESA)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.empresa.cnpj").value("12.345.678/0001-95"))
                .andExpect(jsonPath("$.administrador.usuarioJaExistia").value(false))
                .andReturn().getResponse().getContentAsString();
        Integer empresaId = JsonPath.read(resposta, "$.empresa.id");

        String rosa = "Bearer " + JsonPath.read(login("rosa@bompreco.com", "senhaRosa1", empresaId.longValue())
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString(), "$.tokenDeAcesso");

        mvc.perform(get("/api/v1/autenticacao/eu").header("Authorization", rosa))
                .andExpect(jsonPath("$.permissoes", hasSize(Permissao.values().length)));
        mvc.perform(get("/api/v1/perfis").header("Authorization", rosa))
                .andExpect(jsonPath("$[*].nome", hasItem("Administrador")));
    }

    @Test
    void administradorJaCadastradoEmOutraEmpresaMantemASenha() throws Exception {
        cenario.vincular(cenario.usuario("rosa@bompreco.com"), cenario.empresa("11222333000181", "Outra LTDA"));

        cadastrar(NOVA_EMPRESA)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.administrador.usuarioJaExistia").value(true));

        login("rosa@bompreco.com", Cenario.SENHA, null).andExpect(jsonPath("$.empresas", hasSize(2)));
    }

    @Test
    void cnpjRepetidoOuInvalidoDa422() throws Exception {
        cadastrar(NOVA_EMPRESA).andExpect(status().isCreated());

        cadastrar(NOVA_EMPRESA.replace("rosa@", "outra@")).andExpect(status().isUnprocessableEntity());
        cadastrar(NOVA_EMPRESA.replace("0001-95", "0001-96")).andExpect(status().isUnprocessableEntity());
    }

    @Test
    void seOAdministradorForInvalidoNadaEhGravado() throws Exception {
        cadastrar(NOVA_EMPRESA.replace("senhaRosa1", "123")).andExpect(status().isUnprocessableEntity());

        assertThat(jdbc.queryForObject("SELECT count(*) FROM empresa", Integer.class)).isZero();
        assertThat(jdbc.queryForObject("SELECT count(*) FROM perfil", Integer.class)).isZero();
    }

    @Test
    void validaOCorpo() throws Exception {
        cadastrar("{\"cnpj\":\"\",\"razaoSocial\":\"\"}")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos.cnpj").exists())
                .andExpect(jsonPath("$.campos.administrador").exists());
    }

    @Test
    void soOAdministradorDaPlataformaGerenciaEmpresas() throws Exception {
        var empresa = cenario.empresa("11222333000181", "Empresa A LTDA");
        cenario.vincular(cenario.usuario("dono@a.com"), empresa);
        String donoDaEmpresa = "Bearer " + JsonPath.read(login("dono@a.com", Cenario.SENHA, empresa.id())
                .andReturn().getResponse().getContentAsString(), "$.tokenDeAcesso");

        mvc.perform(get("/api/v1/empresas").header("Authorization", donoDaEmpresa)).andExpect(status().isForbidden());
        mvc.perform(post("/api/v1/empresas").header("Authorization", donoDaEmpresa)
                .contentType(MediaType.APPLICATION_JSON).content(NOVA_EMPRESA)).andExpect(status().isForbidden());
    }

    @Test
    void administradorDaPlataformaNaoVeDadosInternosDasEmpresas() throws Exception {
        cadastrar(NOVA_EMPRESA);

        mvc.perform(get("/api/v1/usuarios").header("Authorization", plataforma)).andExpect(status().isForbidden());
        mvc.perform(get("/api/v1/perfis").header("Authorization", plataforma)).andExpect(status().isForbidden());
    }

    @Test
    void listaAlteraInativaEReativa() throws Exception {
        Integer id = JsonPath.read(cadastrar(NOVA_EMPRESA).andReturn().getResponse().getContentAsString(),
                "$.empresa.id");
        String local = "/api/v1/empresas/" + id;

        mvc.perform(get("/api/v1/empresas").header("Authorization", plataforma)).andExpect(jsonPath("$", hasSize(1)));
        mvc.perform(put(local).header("Authorization", plataforma).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"razaoSocial\":\"Mercado Novo Nome LTDA\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.razaoSocial").value("Mercado Novo Nome LTDA"));

        mvc.perform(delete(local).header("Authorization", plataforma)).andExpect(status().isNoContent());
        login("rosa@bompreco.com", "senhaRosa1", id.longValue())
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.motivo").value("SEM_ACESSO_A_EMPRESA"));

        mvc.perform(post(local + "/reativar").header("Authorization", plataforma))
                .andExpect(jsonPath("$.ativa").value(true));
        login("rosa@bompreco.com", "senhaRosa1", id.longValue()).andExpect(status().isOk());
    }

    @Test
    void empresaInexistenteDa404() throws Exception {
        mvc.perform(get("/api/v1/empresas/999").header("Authorization", plataforma)).andExpect(status().isNotFound());
    }

    private ResultActions cadastrar(String corpo) throws Exception {
        return mvc.perform(post("/api/v1/empresas").header("Authorization", plataforma)
                .contentType(MediaType.APPLICATION_JSON).content(corpo));
    }

    private ResultActions login(String email, String senha, Long empresaId) throws Exception {
        return mvc.perform(post("/api/v1/autenticacao/login").contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"%s\",\"senha\":\"%s\",\"empresaId\":%s}".formatted(email, senha, empresaId)));
    }
}
