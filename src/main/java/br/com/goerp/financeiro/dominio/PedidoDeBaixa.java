package br.com.goerp.financeiro.dominio;

import java.util.Objects;

import br.com.goerp.compartilhado.dominio.Dinheiro;
import br.com.goerp.compartilhado.dominio.excecao.ValorInvalidoException;

/**
 * Um pagamento (ou recebimento) de título, como aconteceu de fato.
 *
 * @param valorPago o que efetivamente saiu ou entrou na conta, já com juros, multa e desconto
 */
public record PedidoDeBaixa(
        Long contaId,
        FormaDePagamento forma,
        Dinheiro valorPago,
        Dinheiro juros,
        Dinheiro multa,
        Dinheiro desconto,
        Long usuarioId) {

    public PedidoDeBaixa {
        Objects.requireNonNull(contaId, "contaId");
        Objects.requireNonNull(forma, "forma");
        Objects.requireNonNull(valorPago, "valorPago");
        juros = Objects.requireNonNullElse(juros, Dinheiro.ZERO).exigirNaoNegativo("juros");
        multa = Objects.requireNonNullElse(multa, Dinheiro.ZERO).exigirNaoNegativo("multa");
        desconto = Objects.requireNonNullElse(desconto, Dinheiro.ZERO).exigirNaoNegativo("desconto");
        if (!forma.movimentaNaHora()) {
            throw new FormaSemMovimentoNaHoraException();
        }
        if (valorPago.compareTo(Dinheiro.ZERO) <= 0) {
            throw new ValorInvalidoException("O valor pago precisa ser maior que zero");
        }
        if (amortizado(valorPago, juros, multa, desconto).compareTo(Dinheiro.ZERO) <= 0) {
            throw new ValorInvalidoException("Juros e multa não podem ser maiores que o valor pago");
        }
    }

    /** Quanto o pagamento abate da dívida: o pago, mais o desconto concedido, menos os acréscimos. */
    public Dinheiro valorAmortizado() {
        return amortizado(valorPago, juros, multa, desconto);
    }

    private static Dinheiro amortizado(Dinheiro pago, Dinheiro juros, Dinheiro multa, Dinheiro desconto) {
        return pago.somar(desconto).subtrair(juros).subtrair(multa);
    }
}
