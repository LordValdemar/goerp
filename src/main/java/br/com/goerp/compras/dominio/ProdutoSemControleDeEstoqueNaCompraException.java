package br.com.goerp.compras.dominio;

import br.com.goerp.compartilhado.dominio.excecao.ExcecaoDeDominio;

public class ProdutoSemControleDeEstoqueNaCompraException extends ExcecaoDeDominio {

    public ProdutoSemControleDeEstoqueNaCompraException(String codigo) {
        super("O produto " + codigo + " não controla estoque e não entra em pedido de compra");
    }
}
