package br.com.goerp.compras.aplicacao;

import java.util.Objects;

import br.com.goerp.compartilhado.aplicacao.CasoDeUso;
import br.com.goerp.compartilhado.dominio.excecao.CampoObrigatorioException;
import br.com.goerp.compartilhado.dominio.excecao.ConflitoDeEdicaoException;
import br.com.goerp.compras.dominio.PedidoDeCompra;
import br.com.goerp.compras.dominio.PedidoDeCompraRepositorio;

/** Só rascunhos são alterados. 409 se outra pessoa alterou desde a leitura. */
@CasoDeUso
public class AlterarPedidoDeCompra {

    private final PedidoDeCompraRepositorio pedidos;
    private final ComprasDaEmpresa compras;

    public AlterarPedidoDeCompra(PedidoDeCompraRepositorio pedidos, ComprasDaEmpresa compras) {
        this.pedidos = pedidos;
        this.compras = compras;
    }

    public PedidoDeCompra executar(Long id, Long versaoLida, DadosDoPedidoDeCompra dados) {
        if (versaoLida == null) {
            throw new CampoObrigatorioException("versao");
        }
        PedidoDeCompra pedido = compras.buscar(id);
        if (!Objects.equals(pedido.versao(), versaoLida)) {
            throw new ConflitoDeEdicaoException("Pedido de compra", id);
        }
        pedido.alterar(compras.conteudo(dados));
        return pedidos.salvar(pedido);
    }
}
