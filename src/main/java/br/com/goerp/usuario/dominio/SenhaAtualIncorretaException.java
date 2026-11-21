package br.com.goerp.usuario.dominio;

import br.com.goerp.compartilhado.dominio.excecao.ExcecaoDeDominio;

public class SenhaAtualIncorretaException extends ExcecaoDeDominio {

    public SenhaAtualIncorretaException() {
        super("A senha atual não confere");
    }
}
