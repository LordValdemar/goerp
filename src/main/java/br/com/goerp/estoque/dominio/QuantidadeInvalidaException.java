package br.com.goerp.estoque.dominio;

import br.com.goerp.compartilhado.dominio.excecao.ExcecaoDeDominio;

public class QuantidadeInvalidaException extends ExcecaoDeDominio {

    public QuantidadeInvalidaException() {
        super("A quantidade da movimentação precisa ser maior que zero");
    }
}
