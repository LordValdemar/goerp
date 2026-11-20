package br.com.goerp.compartilhado.dominio;

import br.com.goerp.compartilhado.dominio.excecao.CestInvalidoException;

/** Código Especificador da Substituição Tributária: 7 dígitos, exigido para alguns NCMs. */
public record Cest(String valor) {

    private static final int DIGITOS = 7;

    public Cest {
        String digitos = Digitos.somente(valor);
        if (digitos.length() != DIGITOS) {
            throw new CestInvalidoException(valor);
        }
        valor = digitos;
    }

    public static Cest de(String valor) {
        return new Cest(valor);
    }
}
