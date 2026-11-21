package br.com.goerp.vendas.aplicacao.pedido;

import java.time.Clock;

import br.com.goerp.cadastros.cliente.aplicacao.BuscarCliente;
import br.com.goerp.compartilhado.aplicacao.CasoDeUso;
import br.com.goerp.compartilhado.aplicacao.EmpresaAtual;
import br.com.goerp.compartilhado.aplicacao.FusoHorario;
import br.com.goerp.compartilhado.aplicacao.UsuarioAtual;
import br.com.goerp.compartilhado.dominio.excecao.ValorInvalidoException;
import br.com.goerp.vendas.dominio.PedidoDeVenda;
import br.com.goerp.vendas.dominio.PedidoDeVendaRepositorio;

/** Cria o pedido como orçamento, para um cliente ativo da empresa. */
@CasoDeUso
public class CriarPedido {

    private final PedidoDeVendaRepositorio pedidos;
    private final PedidosDaEmpresa pedidosDaEmpresa;
    private final BuscarCliente buscarCliente;
    private final EmpresaAtual empresaAtual;
    private final UsuarioAtual usuarioAtual;
    private final FusoHorario fuso;
    private final Clock relogio;

    public CriarPedido(PedidoDeVendaRepositorio pedidos, PedidosDaEmpresa pedidosDaEmpresa, BuscarCliente buscarCliente,
            EmpresaAtual empresaAtual, UsuarioAtual usuarioAtual, FusoHorario fuso, Clock relogio) {
        this.pedidos = pedidos;
        this.pedidosDaEmpresa = pedidosDaEmpresa;
        this.buscarCliente = buscarCliente;
        this.empresaAtual = empresaAtual;
        this.usuarioAtual = usuarioAtual;
        this.fuso = fuso;
        this.relogio = relogio;
    }

    public PedidoDeVenda executar(Long clienteId, DadosDoPedido dados) {
        if (!buscarCliente.executar(clienteId).ativo()) {
            throw new ValorInvalidoException("Cliente inativo não pode receber pedido");
        }
        PedidoDeVenda.Conteudo conteudo = pedidosDaEmpresa.conteudo(dados);
        PedidoDeVenda pedido = PedidoDeVenda.orcar(empresaAtual.id(), pedidos.proximoNumero(empresaAtual.id()),
                clienteId, usuarioAtual.id(), conteudo, pedidosDaEmpresa.politicaDeDesconto(), fuso.hoje(relogio),
                relogio.instant());
        return pedidos.salvar(pedido);
    }
}
