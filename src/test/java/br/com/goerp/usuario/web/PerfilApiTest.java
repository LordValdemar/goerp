package br.com.goerp.usuario.web;

import static br.com.goerp.usuario.dominio.Permissao.PERFIL_LER;
import static br.com.goerp.usuario.dominio.Permissao.USUARIO_LER;
import static org.hamcrest.Matchers.containsInAnyOrder;
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

import br.com.goerp.empresa.dominio.Empresa;
import br.com.goerp.suporte.Cenario;
import br.com.goerp.suporte.Login;
import br.com.goerp.suporte.TesteDeIntegracao;
import br.com.goerp.usuario.dominio.Perfil;
import br.com.goerp.usuario.dominio.Usuario;

class PerfilApiTest extends TesteDeIntegracao {

    private static final String CAIXA = """
            {"nome":"Caixa","permissoes":["USUARIO_LER"]}
            """;

    @Autowired
    MockMvc mvc;

    @Autowired
    Cenario cenario;

    Empresa empresaA;
    Empresa empresaB;
    String administradorDaA;
    String leitorDaA;
    String semPermissaoDaA;
    String administradorDaB;

    @BeforeEach
    void preparar() throws Exception {
        empresaA = cenario.empresa("11222333000181", "Empresa A LTDA");
        empresaB = cenario.empresa("11444777000161", "Empresa B LTDA");

        administradorDaA = entrar("admin.a@exemplo.com", empresaA, cenario.administradorDa(empresaA));
        leitorDaA = entrar("leitor.a@exemplo.com", empresaA, cenario.perfil(empresaA, "Leitor", PERFIL_LER));
        semPermissaoDaA = entrar("caixa.a@exemplo.com", empresaA, cenario.perfil(empresaA, "Operador", USUARIO_LER));
        administradorDaB = entrar("admin.b@exemplo.com", empresaB, cenario.administradorDa(empresaB));
    }

    @Test
    void tokenLevaAsPermissoesDoPerfil() throws Exception {
        mvc.perform(get("/api/v1/autenticacao/eu").header("Authorization", leitorDaA))
                .andExpect(jsonPath("$.permissoes", containsInAnyOrder("PERFIL_LER")));
    }

    @Test
    void semAPermissaoNecessariaRecebe403EmJson() throws Exception {
        mvc.perform(get("/api/v1/perfis").header("Authorization", semPermissaoDaA))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.title").value("Acesso negado"));
    }

    @Test
    void quemSoLeNaoPodeCriar() throws Exception {
        mvc.perform(get("/api/v1/perfis").header("Authorization", leitorDaA)).andExpect(status().isOk());

        criar(leitorDaA, CAIXA).andExpect(status().isForbidden());
    }

    @Test
    void administradorCriaAlteraEInativaPerfil() throws Exception {
        String local = criar(administradorDaA, CAIXA)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nome").value("Caixa"))
                .andReturn().getResponse().getHeader("Location");

        mvc.perform(put(local).header("Authorization", administradorDaA).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"Caixa Sênior\",\"permissoes\":[\"USUARIO_LER\",\"PERFIL_LER\"]}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.permissoes", containsInAnyOrder("USUARIO_LER", "PERFIL_LER")));

        mvc.perform(delete(local).header("Authorization", administradorDaA)).andExpect(status().isNoContent());
        mvc.perform(get(local).header("Authorization", administradorDaA))
                .andExpect(jsonPath("$.ativo").value(false));
    }

    @Test
    void nomeRepetidoNaMesmaEmpresaDa422MasEmOutraEmpresaPode() throws Exception {
        criar(administradorDaA, CAIXA).andExpect(status().isCreated());

        criar(administradorDaA, CAIXA.replace("Caixa", "CAIXA")).andExpect(status().isUnprocessableEntity());
        criar(administradorDaB, CAIXA).andExpect(status().isCreated());
    }

    @Test
    void perfilAdministradorNaoPodeSerAlterado() throws Exception {
        Long administrador = cenario.administradorDa(empresaA).id();

        mvc.perform(put("/api/v1/perfis/" + administrador).header("Authorization", administradorDaA)
                        .contentType(MediaType.APPLICATION_JSON).content(CAIXA))
                .andExpect(status().isUnprocessableEntity());
        mvc.perform(delete("/api/v1/perfis/" + administrador).header("Authorization", administradorDaA))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    void perfilEmUsoNaoPodeSerInativado() throws Exception {
        Perfil leitor = cenario.perfil(empresaA, "Em uso", PERFIL_LER);
        cenario.vincular(cenario.usuario("alguem@exemplo.com"), empresaA, leitor);

        mvc.perform(delete("/api/v1/perfis/" + leitor.id()).header("Authorization", administradorDaA))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    void empresaASoEnxergaEAlteraOsPropriosPerfis() throws Exception {
        Perfil daB = cenario.perfil(empresaB, "Só da B", PERFIL_LER);

        mvc.perform(get("/api/v1/perfis").header("Authorization", administradorDaA))
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[*].nome", containsInAnyOrder("Administrador", "Leitor", "Operador")));
        mvc.perform(get("/api/v1/perfis/" + daB.id()).header("Authorization", administradorDaA))
                .andExpect(status().isNotFound());
        mvc.perform(put("/api/v1/perfis/" + daB.id()).header("Authorization", administradorDaA)
                        .contentType(MediaType.APPLICATION_JSON).content(CAIXA))
                .andExpect(status().isNotFound());
        mvc.perform(delete("/api/v1/perfis/" + daB.id()).header("Authorization", administradorDaA))
                .andExpect(status().isNotFound());
    }

    @Test
    void validaCorpoEPermissaoInexistente() throws Exception {
        criar(administradorDaA, "{\"nome\":\"\",\"permissoes\":[]}")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos.nome").exists())
                .andExpect(jsonPath("$.campos.permissoes").exists());
        criar(administradorDaA, "{\"nome\":\"X\",\"permissoes\":[\"VIRAR_DEUS\"]}")
                .andExpect(status().isBadRequest());
    }

    @Test
    void listaCatalogoDePermissoes() throws Exception {
        mvc.perform(get("/api/v1/permissoes").header("Authorization", leitorDaA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].codigo", hasItem("PERFIL_GERENCIAR")));
    }

    @Test
    void alteracaoDePerfilFicaNoHistorico() throws Exception {
        String local = criar(administradorDaA, CAIXA).andReturn().getResponse().getHeader("Location");
        mvc.perform(put(local).header("Authorization", administradorDaA).contentType(MediaType.APPLICATION_JSON)
                .content("{\"nome\":\"Caixa\",\"permissoes\":[\"PERFIL_LER\"]}"));
        Long id = Long.valueOf(local.substring(local.lastIndexOf('/') + 1));

        var permissoesNoHistorico = jdbc.queryForList(
                "SELECT permissao, revtype FROM perfil_permissao_aud WHERE perfil_id = ? ORDER BY rev, permissao", id);

        org.assertj.core.api.Assertions.assertThat(permissoesNoHistorico)
                .extracting(linha -> linha.get("permissao") + ":" + linha.get("revtype"))
                .containsExactly("USUARIO_LER:0", "PERFIL_LER:0", "USUARIO_LER:2");
    }

    private ResultActions criar(String autorizacao, String corpo) throws Exception {
        return mvc.perform(post("/api/v1/perfis").header("Authorization", autorizacao)
                .contentType(MediaType.APPLICATION_JSON).content(corpo));
    }

    private String entrar(String email, Empresa empresa, Perfil perfil) throws Exception {
        Usuario usuario = cenario.usuario(email);
        cenario.vincular(usuario, empresa, perfil);
        return Login.bearer(mvc, email, empresa.id());
    }
}
