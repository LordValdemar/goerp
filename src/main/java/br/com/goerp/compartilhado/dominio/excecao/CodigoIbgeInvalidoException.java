package br.com.goerp.compartilhado.dominio.excecao;

public class CodigoIbgeInvalidoException extends ExcecaoDeDominio {

    public CodigoIbgeInvalidoException(String valor) {
        super("Código IBGE do município inválido para a UF: " + valor);
    }
}
