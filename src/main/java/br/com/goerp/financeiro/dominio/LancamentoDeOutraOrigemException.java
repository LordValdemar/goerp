package br.com.goerp.financeiro.dominio;

import br.com.goerp.compartilhado.dominio.excecao.ExcecaoDeDominio;

public class LancamentoDeOutraOrigemException extends ExcecaoDeDominio {

    public LancamentoDeOutraOrigemException(OrigemDoLancamento origem) {
        super("Este lançamento veio de " + origem + "; estorne pelo documento que o gerou");
    }
}
