package br.com.goerp.compartilhado.dominio.excecao;

public class NcmInvalidoException extends ExcecaoDeDominio {

    public NcmInvalidoException(String valor) {
        super("NCM inválido (8 dígitos): " + valor);
    }
}
