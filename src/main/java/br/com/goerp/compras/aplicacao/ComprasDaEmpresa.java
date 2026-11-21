package br.com.goerp.compras.aplicacao;

import java.util.Objects;

import br.com.goerp.cadastros.produto.aplicacao.BuscarProduto;
import br.com.goerp.cadastros.produto.aplicacao.ProdutoDetalhado;
import br.com.goerp.compartilhado.aplicacao.ServicoDeAplicacao;
import br.com.goerp.compartilhado.dominio.excecao.RecursoNaoEncontradoException;
import br.com.goerp.compartilhado.dominio.excecao.ValorInvalidoException;
import br.com.goerp.compras.dominio.ItemDeCompra;
import br.com.goerp.compras.dominio.PedidoDeCompra;
import br.com.goerp.compras.dominio.PedidoDeCompraRepositorio;
import br.com.goerp.compras.dominio.ProdutoSemControleDeEstoqueNaCompraException;

/** Busca de pedidos e montagem do conteúdo a partir do cadastro, compartilhadas pelos casos de uso. */
@ServicoDeAplicacao
public class ComprasDaEmpresa {

    private final PedidoDeCompraRepositorio pedidos;
    private final BuscarProduto buscarProduto;

    public ComprasDaEmpresa(PedidoDeCompraRepositorio pedidos, BuscarProduto buscarProduto) {
        this.pedidos = pedidos;
        this.buscarProduto = buscarProduto;
    }

    public PedidoDeCompra buscar(Long id) {
        return pedidos.buscarPorId(id).orElseThrow(() -> new RecursoNaoEncontradoException("Pedido de compra", id));
    }

    public PedidoDeCompra.Conteudo conteudo(DadosDoPedidoDeCompra dados) {
        return new PedidoDeCompra.Conteudo(dados.itens().stream().map(this::item).toList(), dados.previsaoDeEntrega(),
                dados.observacoes());
    }

    /** Produto ativo da empresa, que controla estoque, com quantidade conforme a unidade. */
    private ItemDeCompra item(ItemSolicitadoNaCompra solicitado) {
        ProdutoDetalhado detalhado = buscarProduto.executar(solicitado.produtoId());
        var produto = detalhado.produto();
        if (!produto.ativo()) {
            throw new ValorInvalidoException("O produto " + produto.codigo() + " está inativo");
        }
        if (!produto.dados().controlaEstoque()) {
            throw new ProdutoSemControleDeEstoqueNaCompraException(produto.codigo());
        }
        if (!detalhado.unidade().fracionavel() && !solicitado.quantidade().inteira()) {
            throw new ValorInvalidoException("A unidade do produto " + produto.codigo() + " não aceita fração");
        }
        return ItemDeCompra.novo(produto.id(), produto.codigo(), produto.dados().descricao(), solicitado.quantidade(),
                Objects.requireNonNullElse(solicitado.custoNegociado(), produto.dados().precoDeCusto()));
    }
}
