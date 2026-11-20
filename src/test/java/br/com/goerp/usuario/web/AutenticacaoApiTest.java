package br.com.goerp.usuario.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Duration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import com.jayway.jsonpath.JsonPath;

import br.com.goerp.empresa.dominio.Empresa;
import br.com.goerp.suporte.Cenario;
import br.com.goerp.suporte.TesteDeIntegracao;
import br.com.goerp.usuario.dominio.ControleDeAcesso;
import br.com.goerp.usuario.dominio.Usuario;

class AutenticacaoApiTest extends TesteDeIntegracao {

    @Autowired
    MockMvc mvc;

    @Autowired
    Cenario cenario;

    Empresa empresaA;
    Usuario maria;

    @BeforeEach
    void prepararCenario() {
        empresaA = cenario.empresa("11222333000181", "Empresa A LTDA");
        maria = cenario.usuario("maria@exemplo.com");
        cenario.vincular(maria, empresaA);
    }

    @Test
    void loginDevolveTokensQueDaoAcessoAApi() throws Exception {
        String resposta = login("maria@exemplo.com", Cenario.SENHA, null)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tipo").value("Bearer"))
                .andExpect(jsonPath("$.tokenDeRenovacao", notNullValue()))
                .andExpect(jsonPath("$.empresa.id").value(empresaA.id()))
                .andReturn().getResponse().getContentAsString();

        mvc.perform(get("/api/v1/autenticacao/eu").header("Authorization", "Bearer " + campo(resposta, "$.tokenDeAcesso")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.usuarioId").value(maria.id()))
                .andExpect(jsonPath("$.empresaId").value(empresaA.id()));
    }

    @Test
    void semTokenOuComTokenInvalidoRecebe401EmJson() throws Exception {
        mvc.perform(get("/api/v1/autenticacao/eu"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.title").value("Não autenticado"));

        mvc.perform(get("/api/v1/autenticacao/eu").header("Authorization", "Bearer token.falso.qualquer"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void tokenDeAcessoExpiradoEhRecusado() throws Exception {
        relogio.avancar(Duration.ofHours(-1));
        String tokenDeAcesso = campo(loginComSucesso(), "$.tokenDeAcesso");

        mvc.perform(get("/api/v1/autenticacao/eu").header("Authorization", "Bearer " + tokenDeAcesso))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void senhaErradaDevolve401ComMotivo() throws Exception {
        login("maria@exemplo.com", "errada123", null)
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.motivo").value("CREDENCIAIS_INVALIDAS"));
    }

    @Test
    void depoisDe5ErrosBloqueiaMesmoComSenhaCertaAteOFimDoBloqueio() throws Exception {
        for (int i = 0; i < ControleDeAcesso.MAXIMO_DE_TENTATIVAS; i++) {
            login("maria@exemplo.com", "errada123", null);
        }

        login("maria@exemplo.com", Cenario.SENHA, null)
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.motivo").value("USUARIO_BLOQUEADO"));

        relogio.avancar(ControleDeAcesso.DURACAO_DO_BLOQUEIO);
        login("maria@exemplo.com", Cenario.SENHA, null).andExpect(status().isOk());
    }

    @Test
    void usuarioComVariasEmpresasEscolheEmQualEntrar() throws Exception {
        Empresa empresaB = cenario.empresa("11444777000161", "Empresa B LTDA");
        cenario.vincular(maria, empresaB);

        login("maria@exemplo.com", Cenario.SENHA, null)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.requerSelecaoDeEmpresa").value(true))
                .andExpect(jsonPath("$.empresas", hasSize(2)))
                .andExpect(jsonPath("$.tokenDeAcesso").doesNotExist());

        login("maria@exemplo.com", Cenario.SENHA, empresaB.id())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.empresa.id").value(empresaB.id()));
    }

    @Test
    void naoEntraEmEmpresaDaQualNaoFazParte() throws Exception {
        Empresa outra = cenario.empresa("11444777000161", "Outra LTDA");

        login("maria@exemplo.com", Cenario.SENHA, outra.id())
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.motivo").value("SEM_ACESSO_A_EMPRESA"));
    }

    @Test
    void administradorDaPlataformaEntraSemEmpresaComPermissaoPropria() throws Exception {
        cenario.administradorDaPlataforma("admin@goerp.com");

        String resposta = login("admin@goerp.com", Cenario.SENHA, null)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.empresa", nullValue()))
                .andReturn().getResponse().getContentAsString();

        mvc.perform(get("/api/v1/autenticacao/eu").header("Authorization", "Bearer " + campo(resposta, "$.tokenDeAcesso")))
                .andExpect(jsonPath("$.permissoes", contains("PLATAFORMA_ADMINISTRAR")));
    }

    @Test
    void renovacaoTrocaOsTokensEOTokenAntigoDeixaDeValer() throws Exception {
        String renovacaoAntiga = campo(loginComSucesso(), "$.tokenDeRenovacao");

        String renovada = renovar(renovacaoAntiga)
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        assertThat(campo(renovada, "$.tokenDeRenovacao")).isNotEqualTo(renovacaoAntiga);
        renovar(renovacaoAntiga).andExpect(status().isUnauthorized());
    }

    @Test
    void reusoDeTokenRevogadoEncerraTodasAsSessoes() throws Exception {
        String antigo = campo(loginComSucesso(), "$.tokenDeRenovacao");
        String atual = campo(renovar(antigo).andReturn().getResponse().getContentAsString(), "$.tokenDeRenovacao");

        renovar(antigo).andExpect(status().isUnauthorized());

        renovar(atual).andExpect(status().isUnauthorized());
    }

    @Test
    void sessaoExpiradaNaoRenova() throws Exception {
        String renovacao = campo(loginComSucesso(), "$.tokenDeRenovacao");
        relogio.avancar(Duration.ofDays(7));

        renovar(renovacao)
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.motivo").value("SESSAO_INVALIDA"));
    }

    @Test
    void logoutImpedeRenovacao() throws Exception {
        String renovacao = campo(loginComSucesso(), "$.tokenDeRenovacao");

        mvc.perform(post("/api/v1/autenticacao/sair").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"tokenDeRenovacao\":\"" + renovacao + "\"}"))
                .andExpect(status().isNoContent());

        renovar(renovacao).andExpect(status().isUnauthorized());
    }

    @Test
    void registraEventosDeSegurancaComIp() throws Exception {
        login("maria@exemplo.com", "errada123", null);
        loginComSucesso();

        var eventos = jdbc.queryForList("SELECT tipo, usuario_id, ip FROM evento_seguranca ORDER BY id");

        assertThat(eventos).extracting(e -> e.get("tipo")).containsExactly("LOGIN_FALHA", "LOGIN_SUCESSO");
        assertThat(eventos).allSatisfy(e -> assertThat(e.get("ip")).isNotNull());
    }

    @Test
    void bancoGuardaSoOHashDoTokenDeRenovacao() throws Exception {
        String renovacao = campo(loginComSucesso(), "$.tokenDeRenovacao");

        Integer comTokenEmTextoPuro = jdbc.queryForObject(
                "SELECT count(*) FROM sessao_acesso WHERE hash_token = ?", Integer.class, renovacao);

        assertThat(comTokenEmTextoPuro).isZero();
    }

    @Test
    void validaOCorpoDoLogin() throws Exception {
        mvc.perform(post("/api/v1/autenticacao/login").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos.email", notNullValue()))
                .andExpect(jsonPath("$.campos.senha", notNullValue()));
    }

    private ResultActions login(String email, String senha, Long empresaId) throws Exception {
        String empresa = empresaId == null ? "null" : empresaId.toString();
        return mvc.perform(post("/api/v1/autenticacao/login").contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"email":"%s","senha":"%s","empresaId":%s}
                        """.formatted(email, senha, empresa)));
    }

    private String loginComSucesso() throws Exception {
        return login("maria@exemplo.com", Cenario.SENHA, null)
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
    }

    private ResultActions renovar(String token) throws Exception {
        return mvc.perform(post("/api/v1/autenticacao/renovar").contentType(MediaType.APPLICATION_JSON)
                .content("{\"tokenDeRenovacao\":\"" + token + "\"}"));
    }

    private static String campo(String json, String caminho) {
        return JsonPath.read(json, caminho);
    }
}
