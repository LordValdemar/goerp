package br.com.goerp.vendas.dominio;

import br.com.goerp.compartilhado.dominio.excecao.ExcecaoDeDominio;

public class ProdutoInativoNaVendaException extends ExcecaoDeDominio {

    public ProdutoInativoNaVendaException() {
        super("Produto inativo não pode ser vendido");
    }
}
