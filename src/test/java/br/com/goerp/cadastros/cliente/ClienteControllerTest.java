package br.com.goerp.cadastros.cliente;

import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import br.com.goerp.TestcontainersConfig;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfig.class)
class ClienteControllerTest {

    @Autowired
    MockMvc mvc;

    @Autowired
    ClienteRepository repository;

    @BeforeEach
    void limpar() {
        repository.deleteAll();
    }

    @Test
    void fluxoCompletoDeCliente() throws Exception {
        String json = """
                {"tipoPessoa":"JURIDICA","nome":"Loja Exemplo LTDA","cpfCnpj":"12345678000199"}
                """;

        String location = mvc.perform(post("/api/v1/clientes").contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andReturn().getResponse().getHeader("Location");

        mvc.perform(post("/api/v1/clientes").contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isUnprocessableEntity());

        mvc.perform(delete(location)).andExpect(status().isNoContent());

        mvc.perform(get(location))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ativo").value(false));
    }

    @Test
    void retornaErroDeValidacao() throws Exception {
        mvc.perform(post("/api/v1/clientes").contentType(MediaType.APPLICATION_JSON).content("{\"nome\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos.nome", notNullValue()))
                .andExpect(jsonPath("$.campos.cpfCnpj", notNullValue()));
    }
}
