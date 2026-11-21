package br.com.goerp.financeiro.dominio;

import br.com.goerp.compartilhado.dominio.excecao.ExcecaoDeDominio;

public class CategoriaInativaException extends ExcecaoDeDominio {

    public CategoriaInativaException(String categoria) {
        super("A categoria " + categoria + " está inativa");
    }
}
