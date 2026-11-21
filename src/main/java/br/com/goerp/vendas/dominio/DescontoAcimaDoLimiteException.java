package br.com.goerp.vendas.dominio;

import java.math.BigDecimal;

import br.com.goerp.compartilhado.dominio.excecao.ExcecaoDeDominio;

public class DescontoAcimaDoLimiteException extends ExcecaoDeDominio {

    public DescontoAcimaDoLimiteException(BigDecimal percentualMaximo) {
        super("O desconto passa do máximo permitido de " + percentualMaximo.stripTrailingZeros().toPlainString() + "%");
    }
}
