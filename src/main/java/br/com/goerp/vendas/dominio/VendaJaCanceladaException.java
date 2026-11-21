package br.com.goerp.vendas.dominio;

import br.com.goerp.compartilhado.dominio.excecao.ExcecaoDeDominio;

public class VendaJaCanceladaException extends ExcecaoDeDominio {

    public VendaJaCanceladaException() {
        super("Esta venda já foi cancelada");
    }
}
