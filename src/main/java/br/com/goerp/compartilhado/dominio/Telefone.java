package br.com.goerp.compartilhado.dominio;

import br.com.goerp.compartilhado.dominio.excecao.TelefoneInvalidoException;

/** Telefone com DDD: 10 dígitos (fixo) ou 11 (celular). */
public record Telefone(String valor) {

    private static final int DIGITOS_FIXO = 10;
    private static final int DIGITOS_CELULAR = 11;

    public Telefone {
        String digitos = Digitos.somente(valor);
        if (digitos.length() != DIGITOS_FIXO && digitos.length() != DIGITOS_CELULAR) {
            throw new TelefoneInvalidoException(valor);
        }
        valor = digitos;
    }

    public static Telefone de(String valor) {
        return new Telefone(valor);
    }

    public String formatado() {
        String ddd = valor.substring(0, 2);
        String numero = valor.substring(2);
        int corte = numero.length() - 4;
        return "(" + ddd + ") " + numero.substring(0, corte) + "-" + numero.substring(corte);
    }
}
