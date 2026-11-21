package br.com.goerp.cadastros.fornecedor.web;

import static br.com.goerp.usuario.dominio.Permissao.FORNECEDOR_GERENCIAR;
import static br.com.goerp.usuario.dominio.Permissao.FORNECEDOR_LER;
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

import br.com.goerp.empresa.dominio.Empresa;
import br.com.goerp.suporte.Cenario;
import br.com.goerp.suporte.Login;
import br.com.goerp.suporte.TesteDeIntegracao;
import br.com.goerp.usuario.dominio.Perfil;
import br.com.goerp.usuario.dominio.Permissao;

class FornecedorApiTest extends TesteDeIntegracao {

    private static final String DISTRIBUIDORA = """
            {"documento":"11.444.777/0001-61","nome":"Distribuidora Alfa LTDA","pessoaDeContato":"Carlos Vendedor",
             "telefone":"1133334444","inscricaoEstadual":"110042490114",
             "endereco":{"cep":"30130-010","logradouro":"Av. Afonso Pena","numero":"500","bairro":"Centro",
                         "municipio":"Belo Horizonte","codigoIbgeDoMunicipio":"3106200","uf":"MG"}}
            """;

    @Autowired
    MockMvc mvc;

    @Autowired
    Cenario cenario;

    Empresa empresaA;
    Empresa empresaB;
    String comprasA;
    String comprasB;

    @BeforeEach
    void preparar() throws Exception {
        empresaA = cenario.empresa("11222333000181", "Empresa A LTDA");
        empresaB = cenario.empresa("12345678000195", "Empresa B LTDA");
        comprasA = entrar("compras.a@exemplo.com", empresaA,
                cenario.perfil(empresaA, "Compras", FORNECEDOR_LER, FORNECEDOR_GERENCIAR));
        comprasB = entrar("compras.b@exemplo.com", empresaB,
                cenario.perfil(empresaB, "Compras", FORNECEDOR_LER, FORNECEDOR_GERENCIAR));
    }

    @Test
    void cadastraConsultaEPesquisaPeloContato() throws Exception {
        String local = cadastrar(comprasA, DISTRIBUIDORA)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.tipoDePessoa").value("JURIDICA"))
                .andExpect(jsonPath("$.telefone").value("(11) 3333-4444"))
                .andExpect(jsonPath("$.endereco.uf").value("MG"))
                .andReturn().getResponse().getHeader("Location");

        mvc.perform(get(local).header("Authorization", comprasA)).andExpect(jsonPath("$.pessoaDeContato")
                .value("Carlos Vendedor"));
        pesquisar(comprasA, "?busca=carlos").andExpect(jsonPath("$.itens[*].nome", contains("Distribuidora Alfa LTDA")));
        pesquisar(comprasA, "?busca=11444").andExpect(jsonPath("$.total").value(1));
    }

    @Test
    void documentoRepetidoNaMesmaEmpresaDa422MasEmOutraPode() throws Exception {
        cadastrar(comprasA, DISTRIBUIDORA).andExpect(status().isCreated());

        cadastrar(comprasA, DISTRIBUIDORA).andExpect(status().isUnprocessableEntity());
        cadastrar(comprasB, DISTRIBUIDORA).andExpect(status().isCreated());
    }

    @Test
    void edicaoSimultaneaDa409() throws Exception {
        String local = cadastrar(comprasA, DISTRIBUIDORA).andReturn().getResponse().getHeader("Location");
        String alteracao = DISTRIBUIDORA.replace("\"documento\"", "\"versao\":0,\"documento\"");

        alterar(comprasA, local, alteracao.replace("Carlos", "Ana")).andExpect(status().isOk())
                .andExpect(jsonPath("$.versao").value(1));
        alterar(comprasA, local, alteracao.replace("Carlos", "Pedro")).andExpect(status().isConflict());
    }

    @Test
    void empresaANaoVeNemAlteraFornecedoresDaB() throws Exception {
        String daB = cadastrar(comprasB, DISTRIBUIDORA).andReturn().getResponse().getHeader("Location");

        pesquisar(comprasA, "").andExpect(jsonPath("$.total").value(0));
        mvc.perform(get(daB).header("Authorization", comprasA)).andExpect(status().isNotFound());
        alterar(comprasA, daB, DISTRIBUIDORA.replace("\"documento\"", "\"versao\":0,\"documento\""))
                .andExpect(status().isNotFound());
        mvc.perform(delete(daB).header("Authorization", comprasA)).andExpect(status().isNotFound());
    }

    @Test
    void inativaEReativa() throws Exception {
        String local = cadastrar(comprasA, DISTRIBUIDORA).andReturn().getResponse().getHeader("Location");

        mvc.perform(delete(local).header("Authorization", comprasA)).andExpect(status().isNoContent());
        pesquisar(comprasA, "?ativo=true").andExpect(jsonPath("$.total").value(0));
        mvc.perform(post(local + "/reativar").header("Authorization", comprasA)).andExpect(jsonPath("$.ativo").value(true));
    }

    @Test
    void permissoes() throws Exception {
        String soLeitura = entrar("leitor@exemplo.com", empresaA, cenario.perfil(empresaA, "Leitura", FORNECEDOR_LER));
        String deClientes = entrar("vendas@exemplo.com", empresaA,
                cenario.perfil(empresaA, "Vendas", Permissao.CLIENTE_LER));

        pesquisar(soLeitura, "").andExpect(status().isOk());
        cadastrar(soLeitura, DISTRIBUIDORA).andExpect(status().isForbidden());
        pesquisar(deClientes, "").andExpect(status().isForbidden());
    }

    private ResultActions cadastrar(String token, String corpo) throws Exception {
        return mvc.perform(post("/api/v1/fornecedores").header("Authorization", token)
                .contentType(MediaType.APPLICATION_JSON).content(corpo));
    }

    private ResultActions alterar(String token, String local, String corpo) throws Exception {
        return mvc.perform(put(local).header("Authorization", token).contentType(MediaType.APPLICATION_JSON).content(corpo));
    }

    private ResultActions pesquisar(String token, String parametros) throws Exception {
        return mvc.perform(get("/api/v1/fornecedores" + parametros).header("Authorization", token));
    }

    private String entrar(String email, Empresa empresa, Perfil perfil) throws Exception {
        cenario.vincular(cenario.usuario(email), empresa, perfil);
        return Login.bearer(mvc, email, empresa.id());
    }
}
