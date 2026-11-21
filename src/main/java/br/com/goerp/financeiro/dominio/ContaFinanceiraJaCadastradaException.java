package br.com.goerp.financeiro.dominio;

import br.com.goerp.compartilhado.dominio.excecao.ExcecaoDeDominio;

public class ContaFinanceiraJaCadastradaException extends ExcecaoDeDominio {

    public ContaFinanceiraJaCadastradaException(String nome) {
        super("Já existe uma conta financeira chamada " + nome);
    }
}
