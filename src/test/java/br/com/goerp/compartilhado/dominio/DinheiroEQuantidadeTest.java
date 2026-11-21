package br.com.goerp.compartilhado.dominio;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;

import br.com.goerp.compartilhado.dominio.excecao.ValorInvalidoException;

class DinheiroEQuantidadeTest {

    @Test
    void dinheiroSempreComDuasCasas() {
        assertThat(Dinheiro.de("10").valor()).isEqualTo(new BigDecimal("10.00"));
        assertThat(Dinheiro.de("10.5")).isEqualTo(Dinheiro.de("10.50"));
    }

    @Test
    void naoArredondaEmSilencioOQueFoiDigitado() {
        assertThatThrownBy(() -> Dinheiro.de("10.999")).isInstanceOf(ValorInvalidoException.class);
    }

    @Test
    void somaSemErroDePontoFlutuante() {
        Dinheiro total = Dinheiro.ZERO;
        for (int i = 0; i < 10; i++) {
            total = total.somar(Dinheiro.de("0.10"));
        }

        assertThat(total).isEqualTo(Dinheiro.de("1.00"));
    }

    @Test
    void precoVezesQuantidadeArredondaMeioParaOPar() {
        assertThat(Dinheiro.de("0.25").multiplicar(Quantidade.de("0.5"))).isEqualTo(Dinheiro.de("0.12"));
        assertThat(Dinheiro.de("0.35").multiplicar(Quantidade.de("0.5"))).isEqualTo(Dinheiro.de("0.18"));
        assertThat(Dinheiro.de("12.99").multiplicar(Quantidade.de("1.250"))).isEqualTo(Dinheiro.de("16.24"));
    }

    @Test
    void exigeNaoNegativoQuandoPedido() {
        assertThatThrownBy(() -> Dinheiro.de("-1").exigirNaoNegativo("preco"))
                .isInstanceOf(ValorInvalidoException.class).hasMessageContaining("preco");
    }

    @Test
    void quantidadeComAteTresCasas() {
        assertThat(Quantidade.de("1.25").valor()).isEqualTo(new BigDecimal("1.250"));
        assertThat(Quantidade.de("2").inteira()).isTrue();
        assertThat(Quantidade.de("2.5").inteira()).isFalse();
        assertThatThrownBy(() -> Quantidade.de("1.2345")).isInstanceOf(ValorInvalidoException.class);
    }
}
