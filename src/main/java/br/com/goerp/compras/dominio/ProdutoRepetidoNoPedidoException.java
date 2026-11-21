package br.com.goerp.compras.dominio;

import br.com.goerp.compartilhado.dominio.excecao.ExcecaoDeDominio;

public class ProdutoRepetidoNoPedidoException extends ExcecaoDeDominio {

    public ProdutoRepetidoNoPedidoException() {
        super("Cada produto pode aparecer uma vez só no pedido; ajuste a quantidade");
    }
}
