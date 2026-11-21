package br.com.goerp.compartilhado.dominio.excecao;

public class CestInvalidoException extends ExcecaoDeDominio {

    public CestInvalidoException(String valor) {
        super("CEST inválido (7 dígitos): " + valor);
    }
}
