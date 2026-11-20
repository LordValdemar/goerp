package br.com.goerp.compartilhado.dominio.excecao;

public class EmailInvalidoException extends ExcecaoDeDominio {

    public EmailInvalidoException(String valor) {
        super("E-mail inválido: " + valor);
    }
}
