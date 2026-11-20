package br.com.goerp.suporte;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.jayway.jsonpath.JsonPath;

/** Faz login pela API de verdade e devolve o cabeçalho Authorization pronto para uso. */
public final class Login {

    private Login() {
    }

    public static String bearer(MockMvc mvc, String email, Long empresaId) throws Exception {
        String resposta = mvc.perform(post("/api/v1/autenticacao/login").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"%s","senha":"%s","empresaId":%d}
                                """.formatted(email, Cenario.SENHA, empresaId)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return "Bearer " + JsonPath.read(resposta, "$.tokenDeAcesso");
    }
}
