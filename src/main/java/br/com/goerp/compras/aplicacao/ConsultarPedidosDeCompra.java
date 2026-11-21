package br.com.goerp.compras.aplicacao;

import java.time.Clock;
import java.time.LocalDate;

import br.com.goerp.compartilhado.aplicacao.CasoDeUso;
import br.com.goerp.compartilhado.aplicacao.FusoHorario;
import br.com.goerp.compartilhado.dominio.Pagina;
import br.com.goerp.compartilhado.dominio.Paginacao;
import br.com.goerp.compras.dominio.PedidoDeCompra;
import br.com.goerp.compras.dominio.PedidoDeCompraRepositorio;
import br.com.goerp.compras.dominio.SituacaoDoPedidoDeCompra;

@CasoDeUso
public class ConsultarPedidosDeCompra {

    /** O pedido e se está atrasado hoje (previsão de entrega passou e ainda falta receber). */
    public record PedidoComPrazo(PedidoDeCompra pedido, boolean atrasado) {
    }

    private final PedidoDeCompraRepositorio pedidos;
    private final ComprasDaEmpresa compras;
    private final FusoHorario fuso;
    private final Clock relogio;

    public ConsultarPedidosDeCompra(PedidoDeCompraRepositorio pedidos, ComprasDaEmpresa compras, FusoHorario fuso,
            Clock relogio) {
        this.pedidos = pedidos;
        this.compras = compras;
        this.fuso = fuso;
        this.relogio = relogio;
    }

    public PedidoComPrazo porId(Long id) {
        return comPrazo(compras.buscar(id), fuso.hoje(relogio));
    }

    public Pagina<PedidoComPrazo> pesquisar(Long fornecedorId, SituacaoDoPedidoDeCompra situacao, Paginacao paginacao) {
        LocalDate hoje = fuso.hoje(relogio);
        return pedidos.pesquisar(fornecedorId, situacao, paginacao).mapear(pedido -> comPrazo(pedido, hoje));
    }

    private static PedidoComPrazo comPrazo(PedidoDeCompra pedido, LocalDate hoje) {
        return new PedidoComPrazo(pedido, pedido.atrasado(hoje));
    }
}
