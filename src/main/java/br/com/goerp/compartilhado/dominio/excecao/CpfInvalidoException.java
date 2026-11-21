package br.com.goerp.compartilhado.dominio.excecao;

public class CpfInvalidoException extends ExcecaoDeDominio {

    public CpfInvalidoException(String valor) {
        super("CPF inválido: " + valor);
    }
}
