package br.com.goerp.cadastros.produto.aplicacao;

import br.com.goerp.cadastros.produto.dominio.ProdutoRepositorio;
import br.com.goerp.compartilhado.aplicacao.CasoDeUso;
import br.com.goerp.compartilhado.dominio.excecao.RecursoNaoEncontradoException;

@CasoDeUso
public class BuscarProduto {

    private final ProdutoRepositorio produtos;
    private final DetalhesDeProdutos detalhes;

    public BuscarProduto(ProdutoRepositorio produtos, DetalhesDeProdutos detalhes) {
        this.produtos = produtos;
        this.detalhes = detalhes;
    }

    public ProdutoDetalhado executar(Long id) {
        return produtos.buscarPorId(id)
                .map(detalhes::de)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Produto", id));
    }
}
