package br.com.goerp.cadastros.produto.dominio;

import br.com.goerp.compartilhado.dominio.excecao.ExcecaoDeDominio;

public class UnidadeInativaException extends ExcecaoDeDominio {

    public UnidadeInativaException(String sigla) {
        super("A unidade de medida " + sigla + " está inativa");
    }
}
