package br.com.goerp.vendas.dominio;

import java.math.BigDecimal;

public interface ConfiguracaoDeVendasRepositorio {

    /** Percentual máximo de desconto da empresa; {@link PoliticaDeDesconto#PERCENTUAL_PADRAO} se não configurado. */
    BigDecimal descontoMaximoPercentual();

    void definirDescontoMaximoPercentual(Long empresaId, BigDecimal percentual);
}
