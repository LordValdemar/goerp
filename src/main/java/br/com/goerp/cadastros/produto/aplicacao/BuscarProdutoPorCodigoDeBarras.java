package br.com.goerp.cadastros.produto.aplicacao;

import br.com.goerp.cadastros.produto.dominio.ProdutoRepositorio;
import br.com.goerp.compartilhado.aplicacao.CasoDeUso;
import br.com.goerp.compartilhado.dominio.Gtin;
import br.com.goerp.compartilhado.dominio.excecao.RecursoNaoEncontradoException;

/** Leitura do código de barras no PDV: encontra só produtos ativos. */
@CasoDeUso
public class BuscarProdutoPorCodigoDeBarras {

    private final ProdutoRepositorio produtos;
    private final DetalhesDeProdutos detalhes;

    public BuscarProdutoPorCodigoDeBarras(ProdutoRepositorio produtos, DetalhesDeProdutos detalhes) {
        this.produtos = produtos;
        this.detalhes = detalhes;
    }

    public ProdutoDetalhado executar(String codigoLido) {
        Gtin codigoDeBarras = Gtin.de(codigoLido);
        return produtos.buscarAtivoPorCodigoDeBarras(codigoDeBarras)
                .map(detalhes::de)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Produto com código de barras",
                        codigoDeBarras.valor()));
    }
}
