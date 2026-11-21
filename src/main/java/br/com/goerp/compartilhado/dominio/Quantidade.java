package br.com.goerp.compartilhado.dominio;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

import br.com.goerp.compartilhado.dominio.excecao.ValorInvalidoException;

/** Quantidade com até 3 casas (ex.: 1,250 kg). Valores digitados com mais casas são recusados. */
public record Quantidade(BigDecimal valor) implements Comparable<Quantidade> {

    public static final int CASAS_DECIMAIS = 3;
    public static final Quantidade ZERO = new Quantidade(BigDecimal.ZERO);

    public Quantidade {
        Objects.requireNonNull(valor, "valor");
        if (valor.stripTrailingZeros().scale() > CASAS_DECIMAIS) {
            throw new ValorInvalidoException("Quantidade com mais de 3 casas decimais: " + valor.toPlainString());
        }
        valor = valor.setScale(CASAS_DECIMAIS, RoundingMode.UNNECESSARY);
    }

    public static Quantidade de(String valor) {
        return new Quantidade(new BigDecimal(valor));
    }

    public Quantidade somar(Quantidade outra) {
        return new Quantidade(valor.add(outra.valor));
    }

    public Quantidade subtrair(Quantidade outra) {
        return new Quantidade(valor.subtract(outra.valor));
    }

    public boolean negativa() {
        return valor.signum() < 0;
    }

    public boolean inteira() {
        return valor.stripTrailingZeros().scale() <= 0;
    }

    @Override
    public int compareTo(Quantidade outra) {
        return valor.compareTo(outra.valor);
    }

    @Override
    public String toString() {
        return valor.toPlainString();
    }
}
