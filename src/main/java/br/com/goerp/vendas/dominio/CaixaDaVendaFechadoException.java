package br.com.goerp.vendas.dominio;

import br.com.goerp.compartilhado.dominio.excecao.ExcecaoDeDominio;

public class CaixaDaVendaFechadoException extends ExcecaoDeDominio {

    public CaixaDaVendaFechadoException() {
        super("O caixa desta venda já foi fechado; o cancelamento não é mais possível (use devolução)");
    }
}
