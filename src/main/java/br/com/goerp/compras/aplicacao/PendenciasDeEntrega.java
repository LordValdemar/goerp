package br.com.goerp.compras.aplicacao;

import java.util.List;

import br.com.goerp.compartilhado.dominio.Dinheiro;
import br.com.goerp.compras.dominio.PedidoDeCompra;

/** Pedidos aguardando entrega, o valor que ainda falta chegar e quantos estão atrasados. */
public record PendenciasDeEntrega(List<Pendente> pedidos, Dinheiro valorAReceber, long atrasados) {

    public record Pendente(PedidoDeCompra pedido, boolean atrasado, Dinheiro valorPendente) {
    }
}
