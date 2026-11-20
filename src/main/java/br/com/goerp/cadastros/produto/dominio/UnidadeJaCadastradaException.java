package br.com.goerp.cadastros.produto.dominio;

import br.com.goerp.compartilhado.dominio.excecao.ExcecaoDeDominio;

public class UnidadeJaCadastradaException extends ExcecaoDeDominio {

    public UnidadeJaCadastradaException(String sigla) {
        super("Já existe a unidade de medida " + sigla);
    }
}
