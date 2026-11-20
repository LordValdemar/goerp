package br.com.goerp.compartilhado.dominio;

/** Utilitários para documentos e códigos numéricos digitados com ou sem máscara. */
public final class Digitos {

    private Digitos() {
    }

    public static String somente(String texto) {
        return texto == null ? "" : texto.replaceAll("\\D", "");
    }

    public static boolean todosIguais(String digitos) {
        return digitos.chars().distinct().count() == 1;
    }

    public static int naPosicao(String digitos, int posicao) {
        return digitos.charAt(posicao) - '0';
    }

    /** Dígito verificador "módulo 11" usado por CPF e CNPJ. */
    public static int digitoModulo11(String digitos, int[] pesos) {
        int soma = 0;
        for (int i = 0; i < pesos.length; i++) {
            soma += naPosicao(digitos, i) * pesos[i];
        }
        int resto = soma % 11;
        return resto < 2 ? 0 : 11 - resto;
    }
}
