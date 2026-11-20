package br.com.goerp.estoque.dominio;

import br.com.goerp.compartilhado.dominio.excecao.ExcecaoDeDominio;

public class ProdutoSemControleDeEstoqueException extends ExcecaoDeDominio {

    public ProdutoSemControleDeEstoqueException() {
        super("Este produto não controla estoque");
    }
}
