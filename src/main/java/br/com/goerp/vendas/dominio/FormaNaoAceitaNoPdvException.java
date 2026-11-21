package br.com.goerp.vendas.dominio;

import br.com.goerp.compartilhado.dominio.excecao.ExcecaoDeDominio;
import br.com.goerp.financeiro.dominio.FormaDePagamento;

public class FormaNaoAceitaNoPdvException extends ExcecaoDeDominio {

    public FormaNaoAceitaNoPdvException(FormaDePagamento forma) {
        super("A forma de pagamento " + forma + " não é aceita no PDV");
    }
}
