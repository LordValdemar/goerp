package br.com.goerp.vendas.dominio;

import br.com.goerp.compartilhado.dominio.excecao.ExcecaoDeDominio;

public class TransicaoDePedidoInvalidaException extends ExcecaoDeDominio {

    public TransicaoDePedidoInvalidaException(SituacaoDoPedido atual, String acao) {
        super("Pedido " + atual.name().toLowerCase() + " não pode ser " + acao);
    }
}
