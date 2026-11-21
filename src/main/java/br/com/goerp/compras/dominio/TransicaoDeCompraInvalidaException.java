package br.com.goerp.compras.dominio;

import br.com.goerp.compartilhado.dominio.excecao.ExcecaoDeDominio;

public class TransicaoDeCompraInvalidaException extends ExcecaoDeDominio {

    public TransicaoDeCompraInvalidaException(SituacaoDoPedidoDeCompra atual, String acao) {
        super("Pedido de compra " + atual.descricao() + " não pode ser " + acao);
    }
}
