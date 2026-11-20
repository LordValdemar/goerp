package br.com.goerp.cadastros.produto.aplicacao;

import br.com.goerp.cadastros.produto.dominio.Produto;
import br.com.goerp.cadastros.produto.dominio.ProdutoRepositorio;
import br.com.goerp.compartilhado.aplicacao.CasoDeUso;
import br.com.goerp.compartilhado.dominio.excecao.RecursoNaoEncontradoException;

/** Produto inativo some da busca do PDV e não pode ser vendido; o histórico continua. */
@CasoDeUso
public class AlterarSituacaoDoProduto {

    private final ProdutoRepositorio produtos;
    private final DetalhesDeProdutos detalhes;

    public AlterarSituacaoDoProduto(ProdutoRepositorio produtos, DetalhesDeProdutos detalhes) {
        this.produtos = produtos;
        this.detalhes = detalhes;
    }

    public ProdutoDetalhado executar(Long id, boolean ativar) {
        Produto produto = produtos.buscarPorId(id).orElseThrow(() -> new RecursoNaoEncontradoException("Produto", id));
        if (ativar) {
            produto.ativar();
        } else {
            produto.inativar();
        }
        return detalhes.de(produtos.salvar(produto));
    }
}
