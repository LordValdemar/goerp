package br.com.goerp.compras.dominio;

import br.com.goerp.compartilhado.dominio.Quantidade;
import br.com.goerp.compartilhado.dominio.excecao.ExcecaoDeDominio;

public class QuantidadeAcimaDoPendenteException extends ExcecaoDeDominio {

    public QuantidadeAcimaDoPendenteException(String codigo, Quantidade pendente) {
        super("O produto " + codigo + " tem só " + pendente.valor().stripTrailingZeros().toPlainString()
                + " a receber; ajuste o pedido antes de receber mais");
    }
}
