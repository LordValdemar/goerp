package br.com.goerp.cadastros.produto.dominio;

import br.com.goerp.compartilhado.dominio.excecao.ExcecaoDeDominio;

public class CodigoDeBarrasEmUsoException extends ExcecaoDeDominio {

    public CodigoDeBarrasEmUsoException(String gtin) {
        super("O código de barras " + gtin + " já está em outro produto");
    }
}
