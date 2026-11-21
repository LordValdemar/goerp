package br.com.goerp.vendas.aplicacao.pedido;

import java.time.Clock;
import java.util.Objects;

import br.com.goerp.compartilhado.aplicacao.CasoDeUso;
import br.com.goerp.compartilhado.aplicacao.FusoHorario;
import br.com.goerp.compartilhado.dominio.excecao.CampoObrigatorioException;
import br.com.goerp.compartilhado.dominio.excecao.ConflitoDeEdicaoException;
import br.com.goerp.vendas.dominio.PedidoDeVenda;
import br.com.goerp.vendas.dominio.PedidoDeVendaRepositorio;

/** Só orçamentos são alterados; os preços são recopiados do cadastro. 409 se outra pessoa alterou antes. */
@CasoDeUso
public class AlterarPedido {

    private final PedidoDeVendaRepositorio pedidos;
    private final PedidosDaEmpresa pedidosDaEmpresa;
    private final FusoHorario fuso;
    private final Clock relogio;

    public AlterarPedido(PedidoDeVendaRepositorio pedidos, PedidosDaEmpresa pedidosDaEmpresa, FusoHorario fuso,
            Clock relogio) {
        this.pedidos = pedidos;
        this.pedidosDaEmpresa = pedidosDaEmpresa;
        this.fuso = fuso;
        this.relogio = relogio;
    }

    public PedidoDeVenda executar(Long id, Long versaoLida, DadosDoPedido dados) {
        if (versaoLida == null) {
            throw new CampoObrigatorioException("versao");
        }
        PedidoDeVenda pedido = pedidosDaEmpresa.buscar(id);
        if (!Objects.equals(pedido.versao(), versaoLida)) {
            throw new ConflitoDeEdicaoException("Pedido de venda", id);
        }
        pedido.alterar(pedidosDaEmpresa.conteudo(dados), pedidosDaEmpresa.politicaDeDesconto(), fuso.hoje(relogio));
        return pedidos.salvar(pedido);
    }
}
