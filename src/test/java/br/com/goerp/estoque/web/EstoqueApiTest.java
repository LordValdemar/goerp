package br.com.goerp.estoque.web;

import static br.com.goerp.usuario.dominio.Permissao.ESTOQUE_LER;
import static br.com.goerp.usuario.dominio.Permissao.ESTOQUE_MOVIMENTAR;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;

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

class EstoqueApiTest extends TesteDeIntegracao {

    @Autowired
    MockMvc mvc;

    @Autowired
    Cenario cenario;

    Empresa empresaA;
    String adminA;
    String adminB;
    Integer refrigerante;
    Integer queijo;
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
        servico = produto("SRV", "UN", false);
    }

    @Test
    void entradasESaidasAtualizamSaldoECustoMedio() throws Exception {
        entrada(adminA, refrigerante, "10", "5.00").andExpect(status().isCreated())
                .andExpect(jsonPath("$.tipo").value("ENTRADA"))
                .andExpect(jsonPath("$.saldoResultante").value(10));
        entrada(adminA, refrigerante, "10", "7.00").andExpect(jsonPath("$.custoMedioResultante").value(6.0));

        saida(adminA, refrigerante, "4").andExpect(status().isCreated())
                .andExpect(jsonPath("$.efeitoNoSaldo").value(-4))
                .andExpect(jsonPath("$.custoUnitario").value(6.0))
                .andExpect(jsonPath("$.usuarioId").isNumber());

        mvc.perform(get("/api/v1/estoque/saldos/" + refrigerante).header("Authorization", adminA))
                .andExpect(jsonPath("$.quantidade").value(16))
                .andExpect(jsonPath("$.custoMedio").value(6.0));
    }

    @Test
    void saidaSemSaldoEhRecusadaAteAEmpresaPermitirNegativo() throws Exception {
        entrada(adminA, refrigerante, "3", "5.00");

        saida(adminA, refrigerante, "5").andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.detail").value("Estoque insuficiente: disponível 3, pedido 5"));

        mvc.perform(put("/api/v1/estoque/configuracao").header("Authorization", adminA)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"permiteEstoqueNegativo\":true}"))
                .andExpect(jsonPath("$.permiteEstoqueNegativo").value(true));
        saida(adminA, refrigerante, "5").andExpect(status().isCreated()).andExpect(jsonPath("$.saldoResultante").value(-2));
    }

    @Test
    void regrasDoProduto() throws Exception {
        entrada(adminA, refrigerante, "1.5", "5.00").andExpect(status().isUnprocessableEntity());
        entrada(adminA, queijo, "1.25", "40.00").andExpect(status().isCreated());
        entrada(adminA, servico, "1", "5.00").andExpect(status().isUnprocessableEntity());
        entrada(adminA, refrigerante, "0", "5.00").andExpect(status().isUnprocessableEntity());
    }

    @Test
    void ajusteExigeMotivoEPermissaoPropria() throws Exception {
        entrada(adminA, refrigerante, "10", "5.00");
        String operador = entrar("operador@exemplo.com", "Operador");

        ajuste(operador, refrigerante, "SAIDA", "1", "Quebra").andExpect(status().isForbidden());
        ajuste(adminA, refrigerante, "SAIDA", "1", "").andExpect(status().isBadRequest());
        ajuste(adminA, refrigerante, "SAIDA", "2", "Quebra no transporte").andExpect(status().isCreated())
                .andExpect(jsonPath("$.tipo").value("AJUSTE_DE_SAIDA"))
                .andExpect(jsonPath("$.saldoResultante").value(8));
        saida(operador, refrigerante, "1").andExpect(status().isCreated());
    }

    @Test
    void estornoDesfazUmaVezSo() throws Exception {
        entrada(adminA, refrigerante, "10", "5.00");
        Integer errada = id(entrada(adminA, refrigerante, "10", "7.00"));

        Integer estorno = id(estornar(adminA, errada).andExpect(status().isCreated())
                .andExpect(jsonPath("$.tipo").value("ESTORNO_DE_ENTRADA"))
                .andExpect(jsonPath("$.estornada").value(errada))
                .andExpect(jsonPath("$.saldoResultante").value(10))
                .andExpect(jsonPath("$.custoMedioResultante").value(5.0)));

        estornar(adminA, errada).andExpect(status().isUnprocessableEntity());
        estornar(adminA, estorno).andExpect(status().isUnprocessableEntity());
    }

    @Test
    void empresaBNaoMovimentaNemConsultaOEstoqueDaA() throws Exception {
        Integer movimentacaoDaA = id(entrada(adminA, refrigerante, "10", "5.00"));

        entrada(adminB, refrigerante, "100", "1.00").andExpect(status().isNotFound());
        saida(adminB, refrigerante, "1").andExpect(status().isNotFound());
        estornar(adminB, movimentacaoDaA).andExpect(status().isNotFound());
        mvc.perform(get("/api/v1/estoque/saldos/" + refrigerante).header("Authorization", adminB))
                .andExpect(status().isNotFound());
        mvc.perform(get("/api/v1/estoque/saldos/" + refrigerante).header("Authorization", adminA))
                .andExpect(jsonPath("$.quantidade").value(10));
    }

    @Test
    void saldoGravadoEhSempreASomaDasMovimentacoes() throws Exception {
        entrada(adminA, refrigerante, "10", "5.00");
        saida(adminA, refrigerante, "3");
        ajuste(adminA, refrigerante, "ENTRADA", "2", "Sobra");
        estornar(adminA, id(entrada(adminA, refrigerante, "5", "6.00")));

        BigDecimal soma = jdbc.queryForObject("""
                SELECT sum(CASE WHEN tipo IN ('ENTRADA','AJUSTE_DE_ENTRADA','ESTORNO_DE_SAIDA') THEN quantidade
                                ELSE -quantidade END)
                FROM movimentacao_estoque WHERE produto_id = ?""", BigDecimal.class, refrigerante);
        BigDecimal saldo = jdbc.queryForObject("SELECT quantidade FROM saldo_estoque WHERE produto_id = ?",
                BigDecimal.class, refrigerante);

        assertThat(saldo).isEqualByComparingTo(soma).isEqualByComparingTo("9");
    }

    @Test
    void bancoRecusaAlterarOuApagarMovimentacao() throws Exception {
        Integer movimentacao = id(entrada(adminA, refrigerante, "10", "5.00"));

        assertThatThrownBy(() -> jdbc.update("UPDATE movimentacao_estoque SET quantidade = 99 WHERE id = ?", movimentacao))
                .hasMessageContaining("não podem ser alteradas");
        assertThatThrownBy(() -> jdbc.update("DELETE FROM movimentacao_estoque WHERE id = ?", movimentacao))
                .hasMessageContaining("não podem ser alteradas");
    }

    @Test
    void semPermissaoDeEstoqueRecebe403() throws Exception {
        String soLeitura = cenarioComPerfil("leitor@exemplo.com", "Leitor", ESTOQUE_LER);

        mvc.perform(get("/api/v1/estoque/saldos/" + refrigerante).header("Authorization", soLeitura))
                .andExpect(status().isOk());
        entrada(soLeitura, refrigerante, "1", "1").andExpect(status().isForbidden());
        mvc.perform(put("/api/v1/estoque/configuracao").header("Authorization", soLeitura)
                .contentType(MediaType.APPLICATION_JSON).content("{\"permiteEstoqueNegativo\":true}"))
                .andExpect(status().isForbidden());
    }

    private ResultActions entrada(String token, Integer produto, String quantidade, String custo) throws Exception {
        return enviar("/api/v1/estoque/entradas", token, "{\"produtoId\":%d,\"quantidade\":%s,\"custoUnitario\":%s}"
                .formatted(produto, quantidade, custo));
    }

    private ResultActions saida(String token, Integer produto, String quantidade) throws Exception {
        return enviar("/api/v1/estoque/saidas", token, "{\"produtoId\":%d,\"quantidade\":%s,\"motivo\":\"Consumo\"}"
                .formatted(produto, quantidade));
    }

    private ResultActions ajuste(String token, Integer produto, String sentido, String quantidade, String motivo)
            throws Exception {
        return enviar("/api/v1/estoque/ajustes", token,
                "{\"produtoId\":%d,\"sentido\":\"%s\",\"quantidade\":%s,\"motivo\":\"%s\"}"
                        .formatted(produto, sentido, quantidade, motivo));
    }

    private ResultActions estornar(String token, Integer movimentacao) throws Exception {
        return enviar("/api/v1/estoque/movimentacoes/" + movimentacao + "/estorno", token, "{\"motivo\":\"Lançado errado\"}");
    }

    private ResultActions enviar(String rota, String token, String corpo) throws Exception {
        return mvc.perform(post(rota).header("Authorization", token).contentType(MediaType.APPLICATION_JSON).content(corpo));
    }

    private static Integer id(ResultActions resposta) throws Exception {
        return JsonPath.read(resposta.andReturn().getResponse().getContentAsString(), "$.id");
    }

    private Integer produto(String codigo, String sigla, boolean controlaEstoque) throws Exception {
        Long unidade = jdbc.queryForObject("SELECT id FROM unidade_medida WHERE empresa_id = ? AND sigla = ?", Long.class,
                empresaA.id(), sigla);
        return id(enviar("/api/v1/produtos", adminA, """
                {"codigo":"%s","descricao":"Produto %s","unidadeId":%d,"precoDeVenda":10,"ncm":"22021000",
                 "controlaEstoque":%s}""".formatted(codigo, codigo, unidade, controlaEstoque)));
    }

    private String entrar(String email, String perfil) throws Exception {
        return cenarioComPerfil(email, perfil, ESTOQUE_MOVIMENTAR);
    }

    private String cenarioComPerfil(String email, String perfil, br.com.goerp.usuario.dominio.Permissao permissao)
            throws Exception {
        cenario.vincular(cenario.usuario(email), empresaA, cenario.perfil(empresaA, perfil, permissao));
        return Login.bearer(mvc, email, empresaA.id());
    }
}
