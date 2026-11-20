package br.com.goerp.cadastros.cliente.dominio;

import br.com.goerp.compartilhado.dominio.excecao.ExcecaoDeDominio;

public class InscricaoEstadualException extends ExcecaoDeDominio {

    public InscricaoEstadualException(String mensagem) {
        super(mensagem);
    }
}
