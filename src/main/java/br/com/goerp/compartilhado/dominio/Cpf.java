package br.com.goerp.compartilhado.dominio;

import br.com.goerp.compartilhado.dominio.excecao.CampoObrigatorioException;
import br.com.goerp.compartilhado.dominio.excecao.CpfInvalidoException;

/** CPF sempre válido (11 dígitos e dígitos verificadores corretos); guarda só os dígitos. */
public record Cpf(String valor) implements Documento {

    private static final int[] PESOS_PRIMEIRO_DIGITO = {10, 9, 8, 7, 6, 5, 4, 3, 2};
    private static final int[] PESOS_SEGUNDO_DIGITO = {11, 10, 9, 8, 7, 6, 5, 4, 3, 2};

    public Cpf {
        if (valor == null || valor.isBlank()) {
            throw new CampoObrigatorioException("cpf");
        }
        String digitos = Digitos.somente(valor);
        if (digitos.length() != DIGITOS_DO_CPF || Digitos.todosIguais(digitos)
                || Digitos.digitoModulo11(digitos, PESOS_PRIMEIRO_DIGITO) != Digitos.naPosicao(digitos, 9)
                || Digitos.digitoModulo11(digitos, PESOS_SEGUNDO_DIGITO) != Digitos.naPosicao(digitos, 10)) {
            throw new CpfInvalidoException(valor);
        }
        valor = digitos;
    }

    public static Cpf de(String valor) {
        return new Cpf(valor);
    }

    @Override
    public String formatado() {
        return valor.replaceFirst("(\\d{3})(\\d{3})(\\d{3})(\\d{2})", "$1.$2.$3-$4");
    }

    @Override
    public TipoDePessoa tipoDePessoa() {
        return TipoDePessoa.FISICA;
    }

    @Override
    public String toString() {
        return valor;
    }
}
