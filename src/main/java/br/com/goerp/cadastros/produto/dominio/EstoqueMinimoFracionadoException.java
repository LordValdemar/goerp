package br.com.goerp.cadastros.produto.dominio;

import br.com.goerp.compartilhado.dominio.excecao.ExcecaoDeDominio;

public class EstoqueMinimoFracionadoException extends ExcecaoDeDominio {

    public EstoqueMinimoFracionadoException(String sigla) {
        super("A unidade " + sigla + " não aceita fração: o estoque mínimo precisa ser inteiro");
    }
}
