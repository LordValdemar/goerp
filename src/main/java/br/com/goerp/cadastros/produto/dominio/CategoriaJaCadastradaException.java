package br.com.goerp.cadastros.produto.dominio;

import br.com.goerp.compartilhado.dominio.excecao.ExcecaoDeDominio;

public class CategoriaJaCadastradaException extends ExcecaoDeDominio {

    public CategoriaJaCadastradaException(String nome) {
        super("Já existe a categoria '" + nome + "'");
    }
}
