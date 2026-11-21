package br.com.goerp.compras.dominio;

import br.com.goerp.compartilhado.dominio.excecao.ExcecaoDeDominio;

public class ProdutoForaDoPedidoException extends ExcecaoDeDominio {

    public ProdutoForaDoPedidoException(Long produtoId) {
        super("O produto " + produtoId + " não faz parte deste pedido");
    }
}
