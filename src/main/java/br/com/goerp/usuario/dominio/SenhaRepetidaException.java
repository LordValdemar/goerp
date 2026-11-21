package br.com.goerp.usuario.dominio;

import br.com.goerp.compartilhado.dominio.excecao.ExcecaoDeDominio;

public class SenhaRepetidaException extends ExcecaoDeDominio {

    public SenhaRepetidaException() {
        super("A nova senha deve ser diferente da atual");
    }
}
