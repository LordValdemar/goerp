package br.com.goerp.compras.aplicacao;

import java.time.Clock;

import br.com.goerp.compartilhado.aplicacao.CasoDeUso;
import br.com.goerp.compras.dominio.PedidoDeCompra;
import br.com.goerp.compras.dominio.PedidoDeCompraRepositorio;

/** Enviar ao fornecedor, cancelar (sem recebimento) e encerrar o saldo (com recebimento parcial). */
@CasoDeUso
public class AndamentoDoPedidoDeCompra {

    private final PedidoDeCompraRepositorio pedidos;
    private final ComprasDaEmpresa compras;
    private final Clock relogio;

    public AndamentoDoPedidoDeCompra(PedidoDeCompraRepositorio pedidos, ComprasDaEmpresa compras, Clock relogio) {
        this.pedidos = pedidos;
        this.compras = compras;
        this.relogio = relogio;
    }

    public PedidoDeCompra enviar(Long id) {
        PedidoDeCompra pedido = compras.buscar(id);
        pedido.enviar(relogio.instant());
        return pedidos.salvar(pedido);
    }

    public PedidoDeCompra cancelar(Long id, String motivo) {
        PedidoDeCompra pedido = compras.buscar(id);
        pedido.cancelar(motivo, relogio.instant());
        return pedidos.salvar(pedido);
    }

    public PedidoDeCompra encerrarSaldo(Long id, String motivo) {
        PedidoDeCompra pedido = compras.buscar(id);
        pedido.encerrarSaldo(motivo, relogio.instant());
        return pedidos.salvar(pedido);
    }
}
