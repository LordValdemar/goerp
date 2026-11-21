package br.com.goerp.financeiro.dominio;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;

import br.com.goerp.compartilhado.dominio.Dinheiro;

class ParcelamentoTest {

    private static final LocalDate JANEIRO = LocalDate.of(2027, 1, 31);

    @Test
    void sobraDeCentavosVaiParaAPrimeiraESomaExatamenteOTotal() {
        Parcelamento parcelamento = Parcelamento.mensal(Dinheiro.de("100"), 3, JANEIRO);

        assertThat(parcelamento.parcelas()).extracting(Parcela::valor)
                .containsExactly(Dinheiro.de("33.34"), Dinheiro.de("33.33"), Dinheiro.de("33.33"));
        assertThat(parcelamento.total()).isEqualTo(Dinheiro.de("100"));
        assertThat(parcelamento.parcelas().get(2).numero()).isEqualTo(3);
        assertThat(parcelamento.parcelas().get(2).de()).isEqualTo(3);
    }

    @Test
    void mensalUsaOUltimoDiaQuandoOMesNaoTemODia() {
        assertThat(Parcelamento.mensal(Dinheiro.de("90"), 3, JANEIRO).parcelas()).extracting(Parcela::vencimento)
                .containsExactly(JANEIRO, LocalDate.of(2027, 2, 28), LocalDate.of(2027, 3, 31));
    }

    @Test
    void aVistaEhUmaParcelaSo() {
        assertThat(Parcelamento.aVista(Dinheiro.de("10.50"), JANEIRO).parcelas())
                .containsExactly(new Parcela(1, 1, Dinheiro.de("10.50"), JANEIRO));
    }

    @Test
    void vencimentosEmOrdemSemRepetir() {
        assertThatThrownBy(() -> Parcelamento.comVencimentos(Dinheiro.de("10"), List.of(JANEIRO, JANEIRO)))
                .isInstanceOf(ParcelamentoInvalidoException.class);
        assertThatThrownBy(() -> Parcelamento.comVencimentos(Dinheiro.de("10"), List.of(JANEIRO, JANEIRO.minusDays(1))))
                .isInstanceOf(ParcelamentoInvalidoException.class);
    }

    @Test
    void quantidadeEValorPrecisamFazerSentido() {
        assertThatThrownBy(() -> Parcelamento.mensal(Dinheiro.de("10"), 0, JANEIRO))
                .isInstanceOf(ParcelamentoInvalidoException.class);
        assertThatThrownBy(() -> Parcelamento.mensal(Dinheiro.de("1000"), 61, JANEIRO))
                .isInstanceOf(ParcelamentoInvalidoException.class);
        assertThatThrownBy(() -> Parcelamento.mensal(Dinheiro.de("0.05"), 10, JANEIRO))
                .isInstanceOf(ParcelamentoInvalidoException.class);
        assertThatThrownBy(() -> Parcelamento.aVista(Dinheiro.ZERO, JANEIRO))
                .isInstanceOf(ParcelamentoInvalidoException.class);
    }
}
