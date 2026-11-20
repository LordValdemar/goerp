package br.com.goerp.cadastros.cliente.web;

import static br.com.goerp.usuario.dominio.Permissao.CLIENTE_GERENCIAR;
import static br.com.goerp.usuario.dominio.Permissao.CLIENTE_LER;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.contains;
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

class ClienteApiTest extends TesteDeIntegracao {

    private static final String MARIA = """
            {"documento":"529.982.247-25","nome":"Maria Silva","email":"Maria@Exemplo.com","telefone":"11988887777",
             "limiteDeCredito":1500.50,
             "endereco":{"cep":"01310-100","logradouro":"Av. Paulista","numero":"1000","bairro":"Bela Vista",
                         "municipio":"São Paulo","codigoIbgeDoMunicipio":"3550308","uf":"SP"}}
            """;
    private static final String LOJA = """
            {"documento":"11.222.333/0001-81","nome":"Loja Exemplo LTDA","nomeFantasia":"Lojinha",
             "indicadorDeIe":"CONTRIBUINTE","inscricaoEstadual":"110042490114"}
            """;

    @Autowired
    MockMvc mvc;

    @Autowired
    Cenario cenario;

    Empresa empresaA;
    Empresa empresaB;
    String gerenteA;
    String gerenteB;

    @BeforeEach
    void preparar() throws Exception {
        empresaA = cenario.empresa("11222333000181", "Empresa A LTDA");
        empresaB = cenario.empresa("11444777000161", "Empresa B LTDA");
        gerenteA = entrar("gerente.a@exemplo.com", empresaA, cenario.perfil(empresaA, "Vendas", CLIENTE_LER, CLIENTE_GERENCIAR));
        gerenteB = entrar("gerente.b@exemplo.com", empresaB, cenario.perfil(empresaB, "Vendas", CLIENTE_LER, CLIENTE_GERENCIAR));
    }

    @Test
    void cadastraEConsultaClienteComTodosOsDados() throws Exception {
        String local = cadastrar(gerenteA, MARIA)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.tipoDePessoa").value("FISICA"))
                .andExpect(jsonPath("$.documento").value("529.982.247-25"))
                .andExpect(jsonPath("$.email").value("maria@exemplo.com"))
                .andExpect(jsonPath("$.telefone").value("(11) 98888-7777"))
                .andExpect(jsonPath("$.limiteDeCredito").value(1500.50))
                .andExpect(jsonPath("$.indicadorDeIe").value("NAO_CONTRIBUINTE"))
                .andExpect(jsonPath("$.versao").value(0))
                .andReturn().getResponse().getHeader("Location");

        mvc.perform(get(local).header("Authorization", gerenteA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.endereco.municipio").value("São Paulo"))
                .andExpect(jsonPath("$.endereco.cep").value("01310-100"));
    }

    @Test
    void documentoRepetidoNaMesmaEmpresaDa422MasEmOutraPode() throws Exception {
        cadastrar(gerenteA, MARIA).andExpect(status().isCreated());

        cadastrar(gerenteA, MARIA.replace("529.982.247-25", "52998224725")).andExpect(status().isUnprocessableEntity());
        cadastrar(gerenteB, MARIA).andExpect(status().isCreated());
    }

    @Test
    void regrasDeNegocioViram422() throws Exception {
        cadastrar(gerenteA, MARIA.replace("529.982.247-25", "529.982.247-26"))
                .andExpect(status().isUnprocessableEntity()).andExpect(jsonPath("$.detail").value(
                        org.hamcrest.Matchers.containsString("CPF inválido")));
        cadastrar(gerenteA, MARIA.replace("3550308", "3304557"))
                .andExpect(status().isUnprocessableEntity());
        cadastrar(gerenteA, LOJA.replace("\"inscricaoEstadual\":\"110042490114\"", "\"inscricaoEstadual\":null"))
                .andExpect(status().isUnprocessableEntity());
        cadastrar(gerenteA, MARIA.replace("1500.50", "10.555")).andExpect(status().isUnprocessableEntity());
    }

    @Test
    void validaFormatoDaEntrada() throws Exception {
        cadastrar(gerenteA, "{\"documento\":\"52998224725\",\"nome\":\"\",\"endereco\":{\"cep\":\"\"}}")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos.nome").exists())
                .andExpect(jsonPath("$.campos['endereco.cep']").exists());
    }

    @Test
    void pesquisaPorNomeOuDocumentoComPaginacao() throws Exception {
        cadastrar(gerenteA, MARIA);
        cadastrar(gerenteA, LOJA);
        cadastrar(gerenteA, "{\"documento\":\"11444777000161\",\"nome\":\"Mercado Central\"}");

        pesquisar(gerenteA, "?busca=lojinha").andExpect(jsonPath("$.itens[*].nome", contains("Loja Exemplo LTDA")));
        pesquisar(gerenteA, "?busca=529.982").andExpect(jsonPath("$.itens[*].nome", contains("Maria Silva")));
        pesquisar(gerenteA, "?tamanho=2")
                .andExpect(jsonPath("$.itens", hasSize(2)))
                .andExpect(jsonPath("$.total").value(3))
                .andExpect(jsonPath("$.totalDePaginas").value(2))
                .andExpect(jsonPath("$.itens[*].nome", contains("Loja Exemplo LTDA", "Maria Silva")));
    }

    @Test
    void alteraComAVersaoLidaEDetectaEdicaoSimultanea() throws Exception {
        String local = cadastrar(gerenteA, MARIA).andReturn().getResponse().getHeader("Location");
        String alteracao = MARIA.replace("Maria Silva", "Maria S. Souza").replace("\"documento\"", "\"versao\":0,\"documento\"");

        alterar(gerenteA, local, alteracao)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Maria S. Souza"))
                .andExpect(jsonPath("$.versao").value(1));

        alterar(gerenteA, local, alteracao.replace("Souza", "Outra"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.title").value("Conflito de edição"));
        mvc.perform(get(local).header("Authorization", gerenteA)).andExpect(jsonPath("$.nome").value("Maria S. Souza"));
    }

    @Test
    void inativaEReativaSemApagar() throws Exception {
        String local = cadastrar(gerenteA, MARIA).andReturn().getResponse().getHeader("Location");

        mvc.perform(delete(local).header("Authorization", gerenteA)).andExpect(status().isNoContent());
        pesquisar(gerenteA, "?ativo=true").andExpect(jsonPath("$.total").value(0));
        pesquisar(gerenteA, "?ativo=false").andExpect(jsonPath("$.total").value(1));

        mvc.perform(post(local + "/reativar").header("Authorization", gerenteA)).andExpect(jsonPath("$.ativo").value(true));
    }

    @Test
    void empresaANaoVeNemAlteraClientesDaB() throws Exception {
        String daB = cadastrar(gerenteB, LOJA).andReturn().getResponse().getHeader("Location");

        pesquisar(gerenteA, "").andExpect(jsonPath("$.total").value(0));
        mvc.perform(get(daB).header("Authorization", gerenteA)).andExpect(status().isNotFound());
        alterar(gerenteA, daB, LOJA.replace("\"documento\"", "\"versao\":0,\"documento\"")).andExpect(status().isNotFound());
        mvc.perform(delete(daB).header("Authorization", gerenteA)).andExpect(status().isNotFound());
        mvc.perform(get(daB).header("Authorization", gerenteB)).andExpect(jsonPath("$.ativo").value(true));
    }

    @Test
    void permissoes() throws Exception {
        String soLeitura = entrar("leitor@exemplo.com", empresaA, cenario.perfil(empresaA, "Leitura", CLIENTE_LER));
        String semAcesso = entrar("outro@exemplo.com", empresaA, cenario.perfil(empresaA, "Nada",
                br.com.goerp.usuario.dominio.Permissao.PERFIL_LER));

        pesquisar(soLeitura, "").andExpect(status().isOk());
        cadastrar(soLeitura, MARIA).andExpect(status().isForbidden());
        pesquisar(semAcesso, "").andExpect(status().isForbidden());
    }

    @Test
    void historicoGuardaQuemAlterouOLimiteDeCredito() throws Exception {
        String local = cadastrar(gerenteA, MARIA).andReturn().getResponse().getHeader("Location");
        alterar(gerenteA, local, MARIA.replace("1500.50", "3000").replace("\"documento\"", "\"versao\":0,\"documento\""));
        Long id = Long.valueOf(local.substring(local.lastIndexOf('/') + 1));

        var historico = jdbc.queryForList("""
                SELECT a.limite_credito, r.usuario_id FROM cliente_aud a JOIN revisao r ON r.id = a.rev
                WHERE a.id = ? ORDER BY a.rev""", id);

        assertThat(historico).extracting(linha -> linha.get("limite_credito").toString())
                .containsExactly("1500.50", "3000.00");
        assertThat(historico).allSatisfy(linha -> assertThat(linha.get("usuario_id")).isNotNull());
    }

    private ResultActions cadastrar(String token, String corpo) throws Exception {
        return mvc.perform(post("/api/v1/clientes").header("Authorization", token)
                .contentType(MediaType.APPLICATION_JSON).content(corpo));
    }

    private ResultActions alterar(String token, String local, String corpo) throws Exception {
        return mvc.perform(put(local).header("Authorization", token).contentType(MediaType.APPLICATION_JSON).content(corpo));
    }

    private ResultActions pesquisar(String token, String parametros) throws Exception {
        return mvc.perform(get("/api/v1/clientes" + parametros).header("Authorization", token));
    }

    private String entrar(String email, Empresa empresa, Perfil perfil) throws Exception {
        cenario.vincular(cenario.usuario(email), empresa, perfil);
        return Login.bearer(mvc, email, empresa.id());
    }

}
