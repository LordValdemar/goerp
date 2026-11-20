package br.com.goerp.compartilhado.dominio.excecao;

public class TelefoneInvalidoException extends ExcecaoDeDominio {

    public TelefoneInvalidoException(String valor) {
        super("Telefone inválido (informe DDD + número, 10 ou 11 dígitos): " + valor);
    }
}
