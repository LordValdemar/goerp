package br.com.goerp.usuario.web;

import static br.com.goerp.usuario.dominio.Permissao.PERFIL_LER;
import static br.com.goerp.usuario.dominio.Permissao.USUARIO_LER;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
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
import br.com.goerp.usuario.dominio.Usuario;

class UsuarioApiTest extends TesteDeIntegracao {

    @Autowired
    MockMvc mvc;

    @Autowired
    Cenario cenario;

    Empresa empresaA;
    Empresa empresaB;
    Perfil caixaDaA;
    Usuario adminA;
    String tokenAdminA;

    @BeforeEach
    void preparar() throws Exception {
        empresaA = cenario.empresa("11222333000181", "Empresa A LTDA");
        empresaB = cenario.empresa("11444777000161", "Empresa B LTDA");
        caixaDaA = cenario.perfil(empresaA, "Caixa", USUARIO_LER);
        adminA = cenario.usuario("admin.a@exemplo.com");
        cenario.vincular(adminA, empresaA);
        tokenAdminA = Login.bearer(mvc, "admin.a@exemplo.com", empresaA.id());
    }

    @Test
    void administradorCadastraUsuarioQueConsegueEntrar() throws Exception {
        cadastrar("""
                {"nome":"Joana","email":"joana@exemplo.com","senha":"senhaJoana1","perfilId":%d}
                """.formatted(caixaDaA.id()))
                .andExpect(status().isCreated())
                .andExpect(header().string("X-Usuario-Ja-Existia", "false"))
                .andExpect(jsonPath("$.perfil.nome").value("Caixa"));

        String resposta = mvc.perform(post("/api/v1/autenticacao/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"joana@exemplo.com\",\"senha\":\"senhaJoana1\"}"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        mvc.perform(get("/api/v1/autenticacao/eu").header("Authorization", "Bearer " + JsonPath.read(resposta, "$.tokenDeAcesso")))
                .andExpect(jsonPath("$.permissoes", containsInAnyOrder("USUARIO_LER")));
    }

    @Test
    void usuarioDeOutraEmpresaGanhaAcessoSemTrocarASenha() throws Exception {
        Usuario contador = cenario.usuario("contador@exemplo.com");
        cenario.vincular(contador, empresaB);

        cadastrar("""
                {"email":"contador@exemplo.com","senha":"tentativa999","perfilId":%d}
                """.formatted(caixaDaA.id()))
                .andExpect(status().isCreated())
                .andExpect(header().string("X-Usuario-Ja-Existia", "true"));

        Login.bearer(mvc, "contador@exemplo.com", empresaA.id());
        Login.bearer(mvc, "contador@exemplo.com", empresaB.id());
    }

    @Test
    void naoVinculaQuemJaTemAcesso() throws Exception {
        cadastrar("""
                {"email":"admin.a@exemplo.com","perfilId":%d}
                """.formatted(caixaDaA.id()))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    void semPermissaoDeGerenciarRecebe403() throws Exception {
        Usuario leitor = cenario.usuario("leitor@exemplo.com");
        cenario.vincular(leitor, empresaA, cenario.perfil(empresaA, "Leitor", USUARIO_LER, PERFIL_LER));
        String tokenLeitor = Login.bearer(mvc, "leitor@exemplo.com", empresaA.id());

        mvc.perform(get("/api/v1/usuarios").header("Authorization", tokenLeitor)).andExpect(status().isOk());
        mvc.perform(post("/api/v1/usuarios").header("Authorization", tokenLeitor)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"x@exemplo.com\",\"senha\":\"segredo123\",\"perfilId\":1}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void cadaEmpresaSoVeEGerenciaOsProprios() throws Exception {
        Usuario soDaB = cenario.usuario("so.b@exemplo.com");
        cenario.vincular(soDaB, empresaB);

        mvc.perform(get("/api/v1/usuarios").header("Authorization", tokenAdminA))
                .andExpect(jsonPath("$[*].email", containsInAnyOrder("admin.a@exemplo.com")));
        mvc.perform(get("/api/v1/usuarios/" + soDaB.id()).header("Authorization", tokenAdminA))
                .andExpect(status().isNotFound());
        mvc.perform(delete("/api/v1/usuarios/" + soDaB.id()).header("Authorization", tokenAdminA))
                .andExpect(status().isNotFound());
        mvc.perform(put("/api/v1/usuarios/" + soDaB.id() + "/perfil").header("Authorization", tokenAdminA)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"perfilId\":" + caixaDaA.id() + "}"))
                .andExpect(status().isNotFound());
    }

    @Test
    void naoUsaPerfilDeOutraEmpresa() throws Exception {
        Perfil daB = cenario.perfil(empresaB, "Da B", USUARIO_LER);

        cadastrar("""
                {"nome":"X","email":"x@exemplo.com","senha":"segredo123","perfilId":%d}
                """.formatted(daB.id()))
                .andExpect(status().isNotFound());
    }

    @Test
    void inativarCortaOAcessoNaHoraEReativarDevolve() throws Exception {
        Usuario joana = cenario.usuario("joana@exemplo.com");
        cenario.vincular(joana, empresaA, caixaDaA);
        String renovacaoDaJoana = tokenDeRenovacao("joana@exemplo.com", empresaA.id());

        mvc.perform(delete("/api/v1/usuarios/" + joana.id()).header("Authorization", tokenAdminA))
                .andExpect(status().isNoContent());

        renovar(renovacaoDaJoana).andExpect(status().isUnauthorized());
        login("joana@exemplo.com", Cenario.SENHA, empresaA.id()).andExpect(status().isUnauthorized());

        mvc.perform(post("/api/v1/usuarios/" + joana.id() + "/reativar").header("Authorization", tokenAdminA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ativo").value(true));
        login("joana@exemplo.com", Cenario.SENHA, empresaA.id()).andExpect(status().isOk());
    }

    @Test
    void administradorNaoInativaASiMesmoNemDeixaAEmpresaSemAdministrador() throws Exception {
        mvc.perform(delete("/api/v1/usuarios/" + adminA.id()).header("Authorization", tokenAdminA))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.detail").value("Você não pode inativar o seu próprio acesso"));

        mvc.perform(put("/api/v1/usuarios/" + adminA.id() + "/perfil").header("Authorization", tokenAdminA)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"perfilId\":" + caixaDaA.id() + "}"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.detail").value(
                        "A empresa precisa de pelo menos um usuário ativo com o perfil Administrador"));
    }

    @Test
    void trocaDePerfilValeNoProximoLogin() throws Exception {
        Usuario joana = cenario.usuario("joana@exemplo.com");
        cenario.vincular(joana, empresaA, caixaDaA);
        Perfil leitor = cenario.perfil(empresaA, "Leitor", PERFIL_LER);

        mvc.perform(put("/api/v1/usuarios/" + joana.id() + "/perfil").header("Authorization", tokenAdminA)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"perfilId\":" + leitor.id() + "}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.perfil.nome").value("Leitor"));

        mvc.perform(get("/api/v1/autenticacao/eu")
                        .header("Authorization", Login.bearer(mvc, "joana@exemplo.com", empresaA.id())))
                .andExpect(jsonPath("$.permissoes", containsInAnyOrder("PERFIL_LER")));
    }

    @Test
    void trocaAPropriaSenhaEPrecisaEntrarDeNovo() throws Exception {
        String renovacao = tokenDeRenovacao("admin.a@exemplo.com", empresaA.id());

        trocarSenha(Cenario.SENHA, "novaSenha456").andExpect(status().isNoContent());

        renovar(renovacao).andExpect(status().isUnauthorized());
        login("admin.a@exemplo.com", Cenario.SENHA, empresaA.id()).andExpect(status().isUnauthorized());
        login("admin.a@exemplo.com", "novaSenha456", empresaA.id()).andExpect(status().isOk());
    }

    @Test
    void senhaAtualErradaDa422EContaParaOBloqueio() throws Exception {
        trocarSenha("errada123", "novaSenha456")
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.detail").value("A senha atual não confere"));

        Integer falhas = jdbc.queryForObject("SELECT tentativas_falhas FROM usuario WHERE id = ?", Integer.class,
                adminA.id());
        org.assertj.core.api.Assertions.assertThat(falhas).isEqualTo(1);
    }

    @Test
    void novaSenhaFracaDa422() throws Exception {
        trocarSenha(Cenario.SENHA, "123").andExpect(status().isUnprocessableEntity());
    }

    @Test
    void trocaDeSenhaExigeEstarLogado() throws Exception {
        mvc.perform(post("/api/v1/autenticacao/senha").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"senhaAtual\":\"a\",\"novaSenha\":\"b\"}"))
                .andExpect(status().isUnauthorized());
    }

    private ResultActions cadastrar(String corpo) throws Exception {
        return mvc.perform(post("/api/v1/usuarios").header("Authorization", tokenAdminA)
                .contentType(MediaType.APPLICATION_JSON).content(corpo));
    }

    private ResultActions trocarSenha(String atual, String nova) throws Exception {
        return mvc.perform(post("/api/v1/autenticacao/senha").header("Authorization", tokenAdminA)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"senhaAtual\":\"%s\",\"novaSenha\":\"%s\"}".formatted(atual, nova)));
    }

    private ResultActions login(String email, String senha, Long empresaId) throws Exception {
        return mvc.perform(post("/api/v1/autenticacao/login").contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"%s\",\"senha\":\"%s\",\"empresaId\":%d}".formatted(email, senha, empresaId)));
    }

    private String tokenDeRenovacao(String email, Long empresaId) throws Exception {
        return JsonPath.read(login(email, Cenario.SENHA, empresaId).andReturn().getResponse().getContentAsString(),
                "$.tokenDeRenovacao");
    }

    private ResultActions renovar(String token) throws Exception {
        return mvc.perform(post("/api/v1/autenticacao/renovar").contentType(MediaType.APPLICATION_JSON)
                .content("{\"tokenDeRenovacao\":\"" + token + "\"}"));
    }
}
