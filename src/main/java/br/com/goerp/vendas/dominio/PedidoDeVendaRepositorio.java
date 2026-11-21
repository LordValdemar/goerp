package br.com.goerp.vendas.dominio;

import java.util.Optional;

import br.com.goerp.compartilhado.dominio.Pagina;
import br.com.goerp.compartilhado.dominio.Paginacao;

/** Pedidos de venda da empresa atual. */
public interface PedidoDeVendaRepositorio {

    PedidoDeVenda salvar(PedidoDeVenda pedido);

    Optional<PedidoDeVenda> buscarPorId(Long id);

    /** Do mais recente para o mais antigo; filtros nulos são ignorados. */
    Pagina<PedidoDeVenda> pesquisar(Long clienteId, SituacaoDoPedido situacao, Paginacao paginacao);

    long proximoNumero(Long empresaId);
}
