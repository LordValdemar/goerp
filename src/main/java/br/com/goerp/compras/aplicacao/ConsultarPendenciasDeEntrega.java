package br.com.goerp.compras.aplicacao;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;

import br.com.goerp.compartilhado.aplicacao.CasoDeUso;
import br.com.goerp.compartilhado.aplicacao.FusoHorario;
import br.com.goerp.compartilhado.dominio.Dinheiro;
import br.com.goerp.compras.aplicacao.PendenciasDeEntrega.Pendente;
import br.com.goerp.compras.dominio.PedidoDeCompraRepositorio;

/** O que os fornecedores ainda devem entregar (para cobrar entregas atrasadas e prever o estoque). */
@CasoDeUso
public class ConsultarPendenciasDeEntrega {

    private final PedidoDeCompraRepositorio pedidos;
    private final FusoHorario fuso;
    private final Clock relogio;

    public ConsultarPendenciasDeEntrega(PedidoDeCompraRepositorio pedidos, FusoHorario fuso, Clock relogio) {
        this.pedidos = pedidos;
        this.fuso = fuso;
        this.relogio = relogio;
    }

    public PendenciasDeEntrega executar(Long fornecedorId, boolean somenteAtrasados) {
        LocalDate hoje = fuso.hoje(relogio);
        List<Pendente> pendentes = pedidos.aguardandoEntrega(fornecedorId).stream()
                .map(pedido -> new Pendente(pedido, pedido.atrasado(hoje), pedido.valorPendente()))
                .filter(pendente -> !somenteAtrasados || pendente.atrasado())
                .toList();
        Dinheiro valor = pendentes.stream().map(Pendente::valorPendente).reduce(Dinheiro.ZERO, Dinheiro::somar);
        return new PendenciasDeEntrega(pendentes, valor, pendentes.stream().filter(Pendente::atrasado).count());
    }
}
