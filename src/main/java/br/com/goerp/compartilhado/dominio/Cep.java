package br.com.goerp.compartilhado.dominio;

import br.com.goerp.compartilhado.dominio.excecao.CampoObrigatorioException;
import br.com.goerp.compartilhado.dominio.excecao.CepInvalidoException;

public record Cep(String valor) {

    private static final int DIGITOS = 8;

    public Cep {
        if (valor == null || valor.isBlank()) {
            throw new CampoObrigatorioException("cep");
        }
        String digitos = Digitos.somente(valor);
        if (digitos.length() != DIGITOS || Digitos.todosIguais(digitos)) {
            throw new CepInvalidoException(valor);
        }
        valor = digitos;
    }

    public static Cep de(String valor) {
        return new Cep(valor);
    }

    public String formatado() {
        return valor.substring(0, 5) + "-" + valor.substring(5);
    }
}
