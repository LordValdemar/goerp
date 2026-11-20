package br.com.goerp.compartilhado.aplicacao;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;

class PaginacaoTest {

    @Test
    void limitaOTamanhoEAjustaValoresInvalidos() {
        assertThat(new Paginacao(-1, 0)).isEqualTo(new Paginacao(0, Paginacao.TAMANHO_PADRAO));
        assertThat(new Paginacao(2, 10_000).tamanho()).isEqualTo(Paginacao.TAMANHO_MAXIMO);
    }

    @Test
    void calculaTotalDePaginasEConverteItens() {
        Pagina<Integer> pagina = new Pagina<>(List.of(1, 2), 0, 2, 5);

        assertThat(pagina.totalDePaginas()).isEqualTo(3);
        assertThat(pagina.mapear(String::valueOf).itens()).containsExactly("1", "2");
    }
}
