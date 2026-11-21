package br.com.goerp.financeiro.dominio;

import br.com.goerp.compartilhado.dominio.excecao.ExcecaoDeDominio;

public class ParcelamentoInvalidoException extends ExcecaoDeDominio {

    public ParcelamentoInvalidoException(String mensagem) {
        super(mensagem);
    }
}
