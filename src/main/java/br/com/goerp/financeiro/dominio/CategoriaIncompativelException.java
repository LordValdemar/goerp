package br.com.goerp.financeiro.dominio;

import br.com.goerp.compartilhado.dominio.excecao.ExcecaoDeDominio;

public class CategoriaIncompativelException extends ExcecaoDeDominio {

    public CategoriaIncompativelException(String categoria, TipoDeCategoria tipo) {
        super("A categoria " + categoria + " é de " + tipo.name().toLowerCase() + " e não serve para este lançamento");
    }
}
