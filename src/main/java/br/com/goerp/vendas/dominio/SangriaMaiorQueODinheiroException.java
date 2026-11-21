package br.com.goerp.vendas.dominio;

import br.com.goerp.compartilhado.dominio.excecao.ExcecaoDeDominio;

public class SangriaMaiorQueODinheiroException extends ExcecaoDeDominio {

    public SangriaMaiorQueODinheiroException() {
        super("A sangria é maior que o dinheiro disponível no caixa");
    }
}
