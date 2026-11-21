package br.com.goerp.financeiro.dominio;

import br.com.goerp.compartilhado.dominio.excecao.ExcecaoDeDominio;

public class CategoriaFinanceiraJaCadastradaException extends ExcecaoDeDominio {

    public CategoriaFinanceiraJaCadastradaException(String nome) {
        super("Já existe uma categoria financeira chamada " + nome);
    }
}
