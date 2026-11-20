package br.com.goerp.compartilhado.dominio;

import br.com.goerp.compartilhado.dominio.excecao.CampoObrigatorioException;
import br.com.goerp.compartilhado.dominio.excecao.CnpjInvalidoException;

/**
 * CNPJ sempre válido: só é possível criar com 14 dígitos e dígitos verificadores corretos.
 * Aceita a entrada com ou sem máscara e guarda apenas os dígitos.
 */
public record Cnpj(String valor) implements Documento {

    private static final int[] PESOS_PRIMEIRO_DIGITO = {5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2};
    private static final int[] PESOS_SEGUNDO_DIGITO = {6, 5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2};

    public Cnpj {
        if (valor == null || valor.isBlank()) {
            throw new CampoObrigatorioException("cnpj");
        }
        String digitos = Digitos.somente(valor);
        if (digitos.length() != DIGITOS_DO_CNPJ || Digitos.todosIguais(digitos)
                || Digitos.digitoModulo11(digitos, PESOS_PRIMEIRO_DIGITO) != Digitos.naPosicao(digitos, 12)
                || Digitos.digitoModulo11(digitos, PESOS_SEGUNDO_DIGITO) != Digitos.naPosicao(digitos, 13)) {
            throw new CnpjInvalidoException(valor);
        }
        valor = digitos;
    }

    public static Cnpj de(String valor) {
        return new Cnpj(valor);
    }

    @Override
    public String formatado() {
        return valor.replaceFirst("(\\d{2})(\\d{3})(\\d{3})(\\d{4})(\\d{2})", "$1.$2.$3/$4-$5");
    }

    @Override
    public TipoDePessoa tipoDePessoa() {
        return TipoDePessoa.JURIDICA;
    }

    @Override
    public String toString() {
        return valor;
    }
}
