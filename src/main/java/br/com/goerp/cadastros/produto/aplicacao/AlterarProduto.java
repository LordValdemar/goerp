package br.com.goerp.cadastros.produto.aplicacao;

import java.util.Objects;

import br.com.goerp.cadastros.produto.dominio.Categoria;
import br.com.goerp.cadastros.produto.dominio.CodigoDeBarrasEmUsoException;
import br.com.goerp.cadastros.produto.dominio.DadosDoProduto;
import br.com.goerp.cadastros.produto.dominio.Produto;
import br.com.goerp.cadastros.produto.dominio.ProdutoRepositorio;
import br.com.goerp.cadastros.produto.dominio.UnidadeDeMedida;
import br.com.goerp.compartilhado.aplicacao.CasoDeUso;
import br.com.goerp.compartilhado.dominio.excecao.CampoObrigatorioException;
import br.com.goerp.compartilhado.dominio.excecao.ConflitoDeEdicaoException;
import br.com.goerp.compartilhado.dominio.excecao.RecursoNaoEncontradoException;

/**
 * Altera um produto a partir da versão que o usuário leu (409 se outra pessoa alterou antes).
 * Cada mudança, inclusive de preço, fica no histórico com quem fez e quando.
 */
@CasoDeUso
public class AlterarProduto {

    private final ProdutoRepositorio produtos;
    private final BuscarUnidadeDeMedida buscarUnidade;
    private final BuscarCategoria buscarCategoria;

    public AlterarProduto(ProdutoRepositorio produtos, BuscarUnidadeDeMedida buscarUnidade,
            BuscarCategoria buscarCategoria) {
        this.produtos = produtos;
        this.buscarUnidade = buscarUnidade;
        this.buscarCategoria = buscarCategoria;
    }

    public ProdutoDetalhado executar(Long id, Long versaoLida, DadosDoProduto dados) {
        if (versaoLida == null) {
            throw new CampoObrigatorioException("versao");
        }
        Produto produto = produtos.buscarPorId(id).orElseThrow(() -> new RecursoNaoEncontradoException("Produto", id));
        if (!Objects.equals(produto.versao(), versaoLida)) {
            throw new ConflitoDeEdicaoException("Produto", id);
        }
        if (dados.codigoDeBarras() != null && produtos.codigoDeBarrasEmUso(dados.codigoDeBarras(), id)) {
            throw new CodigoDeBarrasEmUsoException(dados.codigoDeBarras().valor());
        }
        UnidadeDeMedida unidade = buscarUnidade.executar(dados.unidadeId());
        Categoria categoria = dados.categoriaId() == null ? null : buscarCategoria.executar(dados.categoriaId());

        produto.alterar(dados, unidade, categoria);
        return new ProdutoDetalhado(produtos.salvar(produto), unidade, categoria);
    }
}
