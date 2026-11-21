package br.com.goerp.cadastros.produto.dominio;

import br.com.goerp.compartilhado.dominio.excecao.ExcecaoDeDominio;

public class CategoriaInativaException extends ExcecaoDeDominio {

    public CategoriaInativaException(String nome) {
        super("A categoria '" + nome + "' está inativa");
    }
}
