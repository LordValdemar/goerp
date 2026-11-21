package br.com.goerp.estoque.dominio;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;

import org.junit.jupiter.api.Test;

import br.com.goerp.compartilhado.dominio.Quantidade;
import br.com.goerp.compartilhado.dominio.excecao.ValorInvalidoException;

class InventarioTest {

    private static final Instant AGORA = Instant.parse("2026-11-20T12:00:00Z");

    private final Inventario inventario = Inventario.abrir(1L, "Inventário anual", AGORA);

    @Test
    void abreSemContagensERecontagemSubstitui() {
        assertThat(inventario.situacao()).isEqualTo(SituacaoDoInventario.ABERTO);

        inventario.registrarContagem(10L, Quantidade.de("8"));
        inventario.registrarContagem(10L, Quantidade.de("9"));

        assertThat(inventario.contagens()).containsExactlyEntriesOf(java.util.Map.of(10L, Quantidade.de("9")));
    }

    @Test
    void finalizaDevolvendoAsContagens() {
        inventario.registrarContagem(10L, Quantidade.de("8"));

        var contagens = inventario.finalizar(AGORA);

        assertThat(contagens).containsKey(10L);
        assertThat(inventario.situacao()).isEqualTo(SituacaoDoInventario.FINALIZADO);
        assertThat(inventario.encerradoEm()).isEqualTo(AGORA);
    }

    @Test
    void naoFinalizaSemContagem() {
        assertThatThrownBy(() -> inventario.finalizar(AGORA)).isInstanceOf(InventarioSemContagemException.class);
    }

    @Test
    void encerradoNaoAceitaMaisNada() {
        inventario.cancelar(AGORA);

        assertThat(inventario.situacao()).isEqualTo(SituacaoDoInventario.CANCELADO);
        assertThatThrownBy(() -> inventario.registrarContagem(10L, Quantidade.de("1")))
                .isInstanceOf(InventarioEncerradoException.class);
        assertThatThrownBy(() -> inventario.finalizar(AGORA)).isInstanceOf(InventarioEncerradoException.class);
        assertThatThrownBy(() -> inventario.cancelar(AGORA)).isInstanceOf(InventarioEncerradoException.class);
    }

    @Test
    void contagemNegativaEhRecusadaEZeroEhPermitido() {
        assertThatThrownBy(() -> inventario.registrarContagem(10L, Quantidade.de("-1")))
                .isInstanceOf(ValorInvalidoException.class);

        inventario.registrarContagem(10L, Quantidade.ZERO);
        assertThat(inventario.contagens()).containsEntry(10L, Quantidade.ZERO);
    }

    @Test
    void exigeDescricao() {
        assertThatThrownBy(() -> Inventario.abrir(1L, " ", AGORA))
                .isInstanceOf(br.com.goerp.compartilhado.dominio.excecao.CampoObrigatorioException.class);
    }
}
