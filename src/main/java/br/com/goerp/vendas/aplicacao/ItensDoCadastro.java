package br.com.goerp.vendas.aplicacao;

import java.util.List;

import br.com.goerp.cadastros.produto.aplicacao.BuscarProduto;
import br.com.goerp.cadastros.produto.aplicacao.BuscarProdutoPorCodigoDeBarras;
import br.com.goerp.cadastros.produto.aplicacao.ProdutoDetalhado;
import br.com.goerp.compartilhado.aplicacao.ServicoDeAplicacao;
import br.com.goerp.vendas.dominio.ItemDeVenda;
import br.com.goerp.vendas.dominio.ItemSemProdutoException;
import br.com.goerp.vendas.dominio.ProdutoInativoNaVendaException;
import br.com.goerp.vendas.dominio.QuantidadeFracionadaNaVendaException;

/**
 * Monta itens de venda a partir do cadastro (usado pelo PDV e pelo pedido de venda): produto
 * ativo da empresa, quantidade conforme a unidade e preço, código e descrição copiados do cadastro.
 */
@ServicoDeAplicacao
public class ItensDoCadastro {

    private final BuscarProduto buscarProduto;
    private final BuscarProdutoPorCodigoDeBarras buscarPorCodigoDeBarras;

    public ItensDoCadastro(BuscarProduto buscarProduto, BuscarProdutoPorCodigoDeBarras buscarPorCodigoDeBarras) {
        this.buscarProduto = buscarProduto;
        this.buscarPorCodigoDeBarras = buscarPorCodigoDeBarras;
    }

    public List<ItemDeVenda> montar(List<ItemSolicitado> solicitados) {
        return solicitados.stream().map(this::montar).toList();
    }

    public boolean controlaEstoque(Long produtoId) {
        return buscarProduto.executar(produtoId).produto().dados().controlaEstoque();
    }

    private ItemDeVenda montar(ItemSolicitado solicitado) {
        ProdutoDetalhado detalhado = localizar(solicitado);
        var produto = detalhado.produto();
        if (!produto.ativo()) {
            throw new ProdutoInativoNaVendaException();
        }
        if (!detalhado.unidade().fracionavel() && !solicitado.quantidade().inteira()) {
            throw new QuantidadeFracionadaNaVendaException();
        }
        return new ItemDeVenda(produto.id(), produto.codigo(), produto.dados().descricao(), solicitado.quantidade(),
                produto.dados().precoDeVenda(), solicitado.desconto());
    }

    private ProdutoDetalhado localizar(ItemSolicitado solicitado) {
        if (solicitado.produtoId() != null) {
            return buscarProduto.executar(solicitado.produtoId());
        }
        if (solicitado.codigoDeBarras() != null && !solicitado.codigoDeBarras().isBlank()) {
            return buscarPorCodigoDeBarras.executar(solicitado.codigoDeBarras());
        }
        throw new ItemSemProdutoException();
    }
}
