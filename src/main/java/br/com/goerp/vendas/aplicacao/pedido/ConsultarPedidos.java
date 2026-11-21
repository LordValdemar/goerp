package br.com.goerp.vendas.aplicacao.pedido;

import br.com.goerp.compartilhado.aplicacao.CasoDeUso;
import br.com.goerp.compartilhado.dominio.Pagina;
import br.com.goerp.compartilhado.dominio.Paginacao;
import br.com.goerp.vendas.dominio.PedidoDeVenda;
import br.com.goerp.vendas.dominio.PedidoDeVendaRepositorio;
import br.com.goerp.vendas.dominio.SituacaoDoPedido;

@CasoDeUso
public class ConsultarPedidos {

    private final PedidoDeVendaRepositorio pedidos;
    private final PedidosDaEmpresa pedidosDaEmpresa;

    public ConsultarPedidos(PedidoDeVendaRepositorio pedidos, PedidosDaEmpresa pedidosDaEmpresa) {
        this.pedidos = pedidos;
        this.pedidosDaEmpresa = pedidosDaEmpresa;
    }

    public PedidoDeVenda porId(Long id) {
        return pedidosDaEmpresa.buscar(id);
    }

    public Pagina<PedidoDeVenda> pesquisar(Long clienteId, SituacaoDoPedido situacao, Paginacao paginacao) {
        return pedidos.pesquisar(clienteId, situacao, paginacao);
    }
}
