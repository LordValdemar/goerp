package br.com.goerp.vendas.dominio;

import java.util.Objects;

import br.com.goerp.compartilhado.dominio.Dinheiro;
import br.com.goerp.financeiro.dominio.FormaDePagamento;

/** Resultado do fechamento para uma forma de pagamento: diferença positiva é sobra; negativa, falta. */
public record ConferenciaDeCaixa(FormaDePagamento forma, Dinheiro esperado, Dinheiro contado) {

    public ConferenciaDeCaixa {
        Objects.requireNonNull(forma, "forma");
        Objects.requireNonNull(esperado, "esperado");
        Objects.requireNonNull(contado, "contado");
    }

    public Dinheiro diferenca() {
        return contado.subtrair(esperado);
    }

    public boolean confere() {
        return diferenca().compareTo(Dinheiro.ZERO) == 0;
    }
}
