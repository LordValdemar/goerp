package br.com.goerp.compras.dominio;

import br.com.goerp.compartilhado.dominio.excecao.ExcecaoDeDominio;

public class PedidoDeCompraSemItensException extends ExcecaoDeDominio {

    public PedidoDeCompraSemItensException() {
        super("O pedido de compra precisa de pelo menos um item");
    }
}
