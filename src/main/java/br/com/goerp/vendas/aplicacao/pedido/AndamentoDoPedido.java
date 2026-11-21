package br.com.goerp.vendas.aplicacao.pedido;

import java.time.Clock;
import java.util.Comparator;

import br.com.goerp.compartilhado.aplicacao.CasoDeUso;
import br.com.goerp.compartilhado.aplicacao.FusoHorario;
import br.com.goerp.estoque.aplicacao.RegistrarSaida;
import br.com.goerp.estoque.dominio.OrigemDaMovimentacao;
import br.com.goerp.vendas.dominio.ItemDeVenda;
import br.com.goerp.vendas.dominio.PedidoDeVenda;
import br.com.goerp.vendas.dominio.PedidoDeVendaRepositorio;

/**
 * Mudanças de situação do pedido: confirmar, faturar (baixa o estoque na mesma transação, em
 * ordem de produto) e cancelar. Cada método é uma transação.
 */
@CasoDeUso
public class AndamentoDoPedido {

    private final PedidoDeVendaRepositorio pedidos;
    private final PedidosDaEmpresa pedidosDaEmpresa;
    private final RegistrarSaida registrarSaida;
    private final FusoHorario fuso;
    private final Clock relogio;

    public AndamentoDoPedido(PedidoDeVendaRepositorio pedidos, PedidosDaEmpresa pedidosDaEmpresa,
            RegistrarSaida registrarSaida, FusoHorario fuso, Clock relogio) {
        this.pedidos = pedidos;
        this.pedidosDaEmpresa = pedidosDaEmpresa;
        this.registrarSaida = registrarSaida;
        this.fuso = fuso;
        this.relogio = relogio;
    }

    public PedidoDeVenda confirmar(Long id) {
        PedidoDeVenda pedido = pedidosDaEmpresa.buscar(id);
        pedido.confirmar(fuso.hoje(relogio), relogio.instant());
        return pedidos.salvar(pedido);
    }

    /** Fatura e baixa o estoque; se faltar estoque de qualquer item, nada muda. */
    public PedidoDeVenda faturar(Long id) {
        PedidoDeVenda pedido = pedidosDaEmpresa.buscar(id);
        pedido.faturar(relogio.instant());
        PedidoDeVenda faturado = pedidos.salvar(pedido);
        var itensDoCadastro = pedidosDaEmpresa.itensDoCadastro();
        faturado.itens().stream()
                .sorted(Comparator.comparing(ItemDeVenda::produtoId))
                .filter(item -> itensDoCadastro.controlaEstoque(item.produtoId()))
                .forEach(item -> registrarSaida.executar(item.produtoId(), item.quantidade(),
                        OrigemDaMovimentacao.PEDIDO_DE_VENDA, faturado.id(), "Pedido nº " + faturado.numero()));
        return faturado;
    }

    public PedidoDeVenda cancelar(Long id, String motivo) {
        PedidoDeVenda pedido = pedidosDaEmpresa.buscar(id);
        pedido.cancelar(motivo, relogio.instant());
        return pedidos.salvar(pedido);
    }
}
