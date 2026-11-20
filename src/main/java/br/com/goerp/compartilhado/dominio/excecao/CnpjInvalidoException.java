package br.com.goerp.compartilhado.dominio.excecao;

public class CnpjInvalidoException extends ExcecaoDeDominio {

    public CnpjInvalidoException(String valor) {
        super("CNPJ inválido: " + valor);
    }
}
