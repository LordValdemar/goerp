package br.com.goerp.cadastros.produto.dominio;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

import br.com.goerp.compartilhado.dominio.excecao.ValorInvalidoException;

/**
 * Custo de uma unidade do produto, com até 4 casas: custos vêm de compras em caixa ou fardo
 * divididas pela quantidade (ex.: R$ 10,00 / 3 = 3,3333), e 2 casas acumulariam erro.
 */
public record CustoUnitario(BigDecimal valor) {

    public static final int CASAS_DECIMAIS = 4;
    public static final CustoUnitario ZERO = new CustoUnitario(BigDecimal.ZERO);

    public CustoUnitario {
        Objects.requireNonNull(valor, "valor");
        if (valor.stripTrailingZeros().scale() > CASAS_DECIMAIS) {
            throw new ValorInvalidoException("Custo com mais de 4 casas decimais: " + valor.toPlainString());
        }
        if (valor.signum() < 0) {
            throw new ValorInvalidoException("O custo não pode ser negativo");
        }
        valor = valor.setScale(CASAS_DECIMAIS, RoundingMode.UNNECESSARY);
    }

    public static CustoUnitario de(String valor) {
        return new CustoUnitario(new BigDecimal(valor));
    }
}
