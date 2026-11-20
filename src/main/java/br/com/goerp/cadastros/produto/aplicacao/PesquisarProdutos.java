package br.com.goerp.cadastros.produto.aplicacao;

import br.com.goerp.cadastros.produto.dominio.FiltroDeProdutos;
import br.com.goerp.cadastros.produto.dominio.ProdutoRepositorio;
import br.com.goerp.compartilhado.aplicacao.CasoDeUso;
import br.com.goerp.compartilhado.dominio.Pagina;
import br.com.goerp.compartilhado.dominio.Paginacao;

@CasoDeUso
public class PesquisarProdutos {

    private final ProdutoRepositorio produtos;
    private final DetalhesDeProdutos detalhes;

    public PesquisarProdutos(ProdutoRepositorio produtos, DetalhesDeProdutos detalhes) {
        this.produtos = produtos;
        this.detalhes = detalhes;
    }

    public Pagina<ProdutoDetalhado> executar(FiltroDeProdutos filtro, Paginacao paginacao) {
        return detalhes.de(produtos.pesquisar(filtro, paginacao));
    }
}
