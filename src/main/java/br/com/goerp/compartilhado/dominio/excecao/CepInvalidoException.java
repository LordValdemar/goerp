package br.com.goerp.compartilhado.dominio.excecao;

public class CepInvalidoException extends ExcecaoDeDominio {

    public CepInvalidoException(String valor) {
        super("CEP inválido: " + valor);
    }
}
