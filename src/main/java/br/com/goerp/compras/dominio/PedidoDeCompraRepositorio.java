package br.com.goerp.compras.dominio;

import java.util.List;
import java.util.Optional;

import br.com.goerp.compartilhado.dominio.Pagina;
import br.com.goerp.compartilhado.dominio.Paginacao;

/** Pedidos de compra da empresa atual. */
public interface PedidoDeCompraRepositorio {

    PedidoDeCompra salvar(PedidoDeCompra pedido);

    Optional<PedidoDeCompra> buscarPorId(Long id);

    /** Do mais recente para o mais antigo; filtros nulos são ignorados. */
    Pagina<PedidoDeCompra> pesquisar(Long fornecedorId, SituacaoDoPedidoDeCompra situacao, Paginacao paginacao);

    long proximoNumero(Long empresaId);

    /** Enviados ou parcialmente recebidos, pela previsão de entrega (sem previsão por último). */
    List<PedidoDeCompra> aguardandoEntrega(Long fornecedorId);
}
