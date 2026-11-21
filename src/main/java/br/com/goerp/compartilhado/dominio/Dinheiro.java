package br.com.goerp.compartilhado.dominio;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

import br.com.goerp.compartilhado.dominio.excecao.ValorInvalidoException;

/**
 * Valor em reais, sempre com 2 casas. Nunca arredonda em silêncio o que foi digitado:
 * um valor com mais de 2 casas é recusado. Só cálculos (ex.: preço x quantidade) arredondam,
 * com o arredondamento bancário (meio para o par), que não acumula viés em muitas operações.
 */
public record Dinheiro(BigDecimal valor) implements Comparable<Dinheiro> {

    public static final int CASAS_DECIMAIS = 2;
    public static final RoundingMode ARREDONDAMENTO = RoundingMode.HALF_EVEN;
    public static final Dinheiro ZERO = new Dinheiro(BigDecimal.ZERO);

    public Dinheiro {
        Objects.requireNonNull(valor, "valor");
        if (valor.stripTrailingZeros().scale() > CASAS_DECIMAIS) {
            throw new ValorInvalidoException("Valor em dinheiro com mais de 2 casas decimais: " + valor.toPlainString());
        }
        valor = valor.setScale(CASAS_DECIMAIS, ARREDONDAMENTO);
    }

    public static Dinheiro de(String valor) {
        return new Dinheiro(new BigDecimal(valor));
    }

    public static Dinheiro de(BigDecimal valor) {
        return new Dinheiro(valor);
    }

    public Dinheiro somar(Dinheiro outro) {
        return new Dinheiro(valor.add(outro.valor));
    }

    public Dinheiro subtrair(Dinheiro outro) {
        return new Dinheiro(valor.subtract(outro.valor));
    }

    public Dinheiro multiplicar(Quantidade quantidade) {
        return new Dinheiro(valor.multiply(quantidade.valor()).setScale(CASAS_DECIMAIS, ARREDONDAMENTO));
    }

    public boolean negativo() {
        return valor.signum() < 0;
    }

    public Dinheiro exigirNaoNegativo(String campo) {
        if (negativo()) {
            throw new ValorInvalidoException("O campo '" + campo + "' não pode ser negativo");
        }
        return this;
    }

    @Override
    public int compareTo(Dinheiro outro) {
        return valor.compareTo(outro.valor);
    }

    @Override
    public String toString() {
        return valor.toPlainString();
    }
}
