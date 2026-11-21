package br.com.goerp.financeiro.dominio;

import java.time.LocalDate;
import java.util.Objects;

import br.com.goerp.compartilhado.dominio.Dinheiro;

/** Uma parcela de um parcelamento: "2 de 3, R$ 33,33, vence em 10/01". */
public record Parcela(int numero, int de, Dinheiro valor, LocalDate vencimento) {

    public Parcela {
        Objects.requireNonNull(valor, "valor");
        Objects.requireNonNull(vencimento, "vencimento");
        if (numero < 1 || numero > de) {
            throw new IllegalArgumentException("Parcela " + numero + " de " + de);
        }
    }
}
