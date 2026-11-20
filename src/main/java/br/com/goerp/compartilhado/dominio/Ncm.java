package br.com.goerp.compartilhado.dominio;

import br.com.goerp.compartilhado.dominio.excecao.NcmInvalidoException;

/** Nomenclatura Comum do Mercosul: classificação fiscal do produto, 8 dígitos. */
public record Ncm(String valor) {

    private static final int DIGITOS = 8;

    public Ncm {
        String digitos = Digitos.somente(valor);
        if (digitos.length() != DIGITOS) {
            throw new NcmInvalidoException(valor);
        }
        valor = digitos;
    }

    public static Ncm de(String valor) {
        return new Ncm(valor);
    }
}
