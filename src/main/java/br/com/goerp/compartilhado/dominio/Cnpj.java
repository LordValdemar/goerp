package br.com.goerp.compartilhado.dominio;

import br.com.goerp.compartilhado.dominio.excecao.CampoObrigatorioException;
import br.com.goerp.compartilhado.dominio.excecao.CnpjInvalidoException;

/**
 * CNPJ sempre válido: só é possível criar com 14 dígitos e dígitos verificadores corretos.
 * Aceita a entrada com ou sem máscara e guarda apenas os dígitos.
 */
public record Cnpj(String valor) {

    private static final int[] PESOS_PRIMEIRO_DIGITO = {5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2};
    private static final int[] PESOS_SEGUNDO_DIGITO = {6, 5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2};

    public Cnpj {
        if (valor == null || valor.isBlank()) {
            throw new CampoObrigatorioException("cnpj");
        }
        String digitos = valor.replaceAll("\\D", "");
        if (!possuiFormatoValido(digitos) || !possuiDigitosVerificadoresValidos(digitos)) {
            throw new CnpjInvalidoException(valor);
        }
        valor = digitos;
    }

    public static Cnpj de(String valor) {
        return new Cnpj(valor);
    }

    public String formatado() {
        return valor.replaceFirst("(\\d{2})(\\d{3})(\\d{3})(\\d{4})(\\d{2})", "$1.$2.$3/$4-$5");
    }

    private static boolean possuiFormatoValido(String digitos) {
        boolean todosIguais = digitos.chars().distinct().count() == 1;
        return digitos.length() == 14 && !todosIguais;
    }

    private static boolean possuiDigitosVerificadoresValidos(String digitos) {
        return calcularDigito(digitos, PESOS_PRIMEIRO_DIGITO) == digitoNaPosicao(digitos, 12)
                && calcularDigito(digitos, PESOS_SEGUNDO_DIGITO) == digitoNaPosicao(digitos, 13);
    }

    private static int calcularDigito(String digitos, int[] pesos) {
        int soma = 0;
        for (int i = 0; i < pesos.length; i++) {
            soma += digitoNaPosicao(digitos, i) * pesos[i];
        }
        int resto = soma % 11;
        return resto < 2 ? 0 : 11 - resto;
    }

    private static int digitoNaPosicao(String digitos, int posicao) {
        return digitos.charAt(posicao) - '0';
    }

    @Override
    public String toString() {
        return valor;
    }
}
