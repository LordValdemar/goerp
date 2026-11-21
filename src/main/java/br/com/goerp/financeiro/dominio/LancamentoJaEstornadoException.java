package br.com.goerp.financeiro.dominio;

import br.com.goerp.compartilhado.dominio.excecao.ExcecaoDeDominio;

public class LancamentoJaEstornadoException extends ExcecaoDeDominio {

    public LancamentoJaEstornadoException() {
        super("Este lançamento já foi estornado");
    }
}
