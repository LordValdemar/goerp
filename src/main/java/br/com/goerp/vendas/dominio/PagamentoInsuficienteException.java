package br.com.goerp.vendas.dominio;

import br.com.goerp.compartilhado.dominio.excecao.ExcecaoDeDominio;

public class PagamentoInsuficienteException extends ExcecaoDeDominio {

    public PagamentoInsuficienteException() {
        super("Os pagamentos não cobrem o total da venda");
    }
}
