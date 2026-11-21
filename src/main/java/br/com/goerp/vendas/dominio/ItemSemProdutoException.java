package br.com.goerp.vendas.dominio;

import br.com.goerp.compartilhado.dominio.excecao.ExcecaoDeDominio;

public class ItemSemProdutoException extends ExcecaoDeDominio {

    public ItemSemProdutoException() {
        super("Cada item precisa do produto ou do código de barras");
    }
}
