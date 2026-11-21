package br.com.goerp.vendas.dominio;

import java.math.BigDecimal;
import java.util.Objects;

import br.com.goerp.compartilhado.dominio.excecao.ValorInvalidoException;

/**
 * @param percentualMaximo   configurado pela empresa (0 a 100) sobre o valor bruto da venda
 * @param operadorPodeDescontar o operador tem a permissão de dar desconto
 */
public record PoliticaDeDesconto(BigDecimal percentualMaximo, boolean operadorPodeDescontar) {

    public static final BigDecimal PERCENTUAL_PADRAO = BigDecimal.TEN;
    private static final BigDecimal CEM = BigDecimal.valueOf(100);

    public PoliticaDeDesconto {
        Objects.requireNonNull(percentualMaximo, "percentualMaximo");
        if (percentualMaximo.signum() < 0 || percentualMaximo.compareTo(CEM) > 0) {
            throw new ValorInvalidoException("O desconto máximo precisa estar entre 0% e 100%");
        }
    }

    /** Maior desconto permitido sobre um valor bruto. */
    public BigDecimal limiteSobre(BigDecimal valorBruto) {
        return valorBruto.multiply(percentualMaximo).divide(CEM);
    }
}
