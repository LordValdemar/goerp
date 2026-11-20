package br.com.goerp.cadastros.produto.dominio;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import br.com.goerp.compartilhado.dominio.excecao.CampoObrigatorioException;

class UnidadeDeMedidaECategoriaTest {

    @Test
    void siglaFicaEmMaiusculasESemEspacos() {
        assertThat(UnidadeDeMedida.nova(1L, " kg ", "Quilograma", true).sigla()).isEqualTo("KG");
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "  ", "CAIXA12", "K G", "KG!"})
    void rejeitaSiglaInvalida(String sigla) {
        assertThatThrownBy(() -> UnidadeDeMedida.nova(1L, sigla, "X", false)).isInstanceOf(SiglaInvalidaException.class);
    }

    @Test
    void listaPadraoTemUnidadesFracionaveisEInteiras() {
        var padroes = UnidadeDeMedida.padroesDa(1L);

        assertThat(padroes).extracting(UnidadeDeMedida::sigla)
                .containsExactly("UN", "KG", "G", "L", "ML", "M", "CX", "PC", "PCT", "DZ");
        assertThat(padroes).filteredOn(UnidadeDeMedida::fracionavel).extracting(UnidadeDeMedida::sigla)
                .containsExactly("KG", "G", "L", "ML", "M");
        assertThat(padroes).allMatch(UnidadeDeMedida::ativa);
    }

    @Test
    void alteraDescricaoEFracaoMasExigeDescricao() {
        UnidadeDeMedida unidade = UnidadeDeMedida.nova(1L, "FD", "Fardo", false);

        unidade.alterar("Fardo com 12", true);
        assertThat(unidade.descricao()).isEqualTo("Fardo com 12");
        assertThat(unidade.fracionavel()).isTrue();
        assertThatThrownBy(() -> unidade.alterar(" ", false)).isInstanceOf(CampoObrigatorioException.class);
    }

    @Test
    void categoriaExigeNomeEPodeSerInativada() {
        assertThatThrownBy(() -> Categoria.nova(1L, "")).isInstanceOf(CampoObrigatorioException.class);

        Categoria bebidas = Categoria.nova(1L, " Bebidas ");
        bebidas.definirSituacao(false);

        assertThat(bebidas.nome()).isEqualTo("Bebidas");
        assertThat(bebidas.ativa()).isFalse();
    }
}
