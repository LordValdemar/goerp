package br.com.goerp.vendas.web;

import static br.com.goerp.usuario.dominio.Permissao.CAIXA_GERENCIAR;
import static br.com.goerp.usuario.dominio.Permissao.CAIXA_OPERAR;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasSize;
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

class CaixaApiTest extends TesteDeIntegracao {

    @Autowired
    MockMvc mvc;

    @Autowired
    Cenario cenario;

    Empresa empresaA;
    String operadora;
    String outroOperador;
    String gerente;
    String operadorDaB;

    @BeforeEach
    void preparar() throws Exception {
        empresaA = cenario.empresa("11222333000181", "Empresa A LTDA");
        Empresa empresaB = cenario.empresa("12345678000195", "Empresa B LTDA");
        Perfil caixa = cenario.perfil(empresaA, "Caixa", CAIXA_OPERAR);
        operadora = entrar("ana@exemplo.com", empresaA, caixa);
        outroOperador = entrar("bruno@exemplo.com", empresaA, caixa);
        gerente = entrar("gerente@exemplo.com", empresaA, cenario.perfil(empresaA, "Gerente", CAIXA_OPERAR, CAIXA_GERENCIAR));
        operadorDaB = entrar("carla@exemplo.com", empresaB, cenario.perfil(empresaB, "Caixa", CAIXA_OPERAR));
    }

    @Test
    void abreUmCaixaPorOperadorComOTrocoEsperado() throws Exception {
        abrir(operadora, "100.00").andExpect(status().isCreated())
                .andExpect(jsonPath("$.situacao").value("ABERTO"))
                .andExpect(jsonPath("$.esperado.DINHEIRO").value(100.0))
                .andExpect(jsonPath("$.esperado.PIX").value(0));

        abrir(operadora, "50").andExpect(status().isUnprocessableEntity());
        abrir(outroOperador, "50").andExpect(status().isCreated());
        mvc.perform(get("/api/v1/caixas/meu").header("Authorization", operadora))
                .andExpect(jsonPath("$.trocoInicial").value(100.0));
    }

    @Test
    void gerenteLancaSuprimentoESangriaEOperadorNao() throws Exception {
        Integer caixa = id(abrir(operadora, "100"));

        movimentar(operadora, caixa, "sangrias", "10", "Cofre").andExpect(status().isForbidden());
        movimentar(gerente, caixa, "suprimentos", "50", "Reforço de troco").andExpect(status().isOk());
        movimentar(gerente, caixa, "sangrias", "120", "Cofre").andExpect(status().isOk())
                .andExpect(jsonPath("$.esperado.DINHEIRO").value(30.0))
                .andExpect(jsonPath("$.movimentos[*].tipo", contains("SUPRIMENTO", "SANGRIA")));

        movimentar(gerente, caixa, "sangrias", "30.01", "Cofre").andExpect(status().isUnprocessableEntity());
        movimentar(gerente, caixa, "sangrias", "5", "").andExpect(status().isUnprocessableEntity());
    }

    @Test
    void fechaComConferenciaEDepoisPodeAbrirOutro() throws Exception {
        Integer caixa = id(abrir(operadora, "100"));

        fechar(operadora, caixa, "{\"DINHEIRO\":98.50}").andExpect(status().isOk())
                .andExpect(jsonPath("$.situacao").value("FECHADO"))
                .andExpect(jsonPath("$.conferencias", hasSize(4)))
                .andExpect(jsonPath("$.conferencias[?(@.forma == 'DINHEIRO')].diferenca", contains(-1.5)));

        fechar(operadora, caixa, "{}").andExpect(status().isUnprocessableEntity());
        mvc.perform(get("/api/v1/caixas/meu").header("Authorization", operadora)).andExpect(status().isNotFound());
        abrir(operadora, "100").andExpect(status().isCreated());
    }

    @Test
    void operadorNaoVeNemFechaOCaixaDeOutroMasOGerentePode() throws Exception {
        Integer caixaDaAna = id(abrir(operadora, "100"));

        mvc.perform(get("/api/v1/caixas/" + caixaDaAna).header("Authorization", outroOperador))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.detail").value("Este caixa é de outro operador"));
        fechar(outroOperador, caixaDaAna, "{}").andExpect(status().isForbidden());

        mvc.perform(get("/api/v1/caixas?situacao=ABERTO").header("Authorization", gerente))
                .andExpect(jsonPath("$", hasSize(1)));
        fechar(gerente, caixaDaAna, "{\"DINHEIRO\":100}").andExpect(status().isOk())
                .andExpect(jsonPath("$.fechadoPor").isNumber());
        mvc.perform(get("/api/v1/caixas").header("Authorization", outroOperador)).andExpect(status().isForbidden());
    }

    @Test
    void outraEmpresaNaoEnxergaOCaixa() throws Exception {
        Integer caixa = id(abrir(operadora, "100"));

        mvc.perform(get("/api/v1/caixas/" + caixa).header("Authorization", operadorDaB)).andExpect(status().isNotFound());
        fechar(operadorDaB, caixa, "{}").andExpect(status().isNotFound());
        abrir(operadorDaB, "10").andExpect(status().isCreated());
    }

    @Test
    void trocoNegativoEhRecusado() throws Exception {
        abrir(operadora, "-1").andExpect(status().isUnprocessableEntity());
    }

    private ResultActions abrir(String token, String troco) throws Exception {
        return mvc.perform(post("/api/v1/caixas").header("Authorization", token).contentType(MediaType.APPLICATION_JSON)
                .content("{\"trocoInicial\":" + troco + "}"));
    }

    private ResultActions movimentar(String token, Integer caixa, String tipo, String valor, String motivo)
            throws Exception {
        return mvc.perform(post("/api/v1/caixas/%d/%s".formatted(caixa, tipo)).header("Authorization", token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"valor\":%s,\"motivo\":\"%s\"}".formatted(valor, motivo)));
    }

    private ResultActions fechar(String token, Integer caixa, String contado) throws Exception {
        return mvc.perform(post("/api/v1/caixas/%d/fechar".formatted(caixa)).header("Authorization", token)
                .contentType(MediaType.APPLICATION_JSON).content("{\"contado\":" + contado + "}"));
    }

    private static Integer id(ResultActions resposta) throws Exception {
        return JsonPath.read(resposta.andReturn().getResponse().getContentAsString(), "$.id");
    }

    private String entrar(String email, Empresa empresa, Perfil perfil) throws Exception {
        cenario.vincular(cenario.usuario(email), empresa, perfil);
        return Login.bearer(mvc, email, empresa.id());
    }
}
