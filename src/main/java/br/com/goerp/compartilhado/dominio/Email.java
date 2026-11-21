package br.com.goerp.compartilhado.dominio;

import java.util.Locale;
import java.util.regex.Pattern;

import br.com.goerp.compartilhado.dominio.excecao.CampoObrigatorioException;
import br.com.goerp.compartilhado.dominio.excecao.EmailInvalidoException;

/** E-mail sempre válido e normalizado (sem espaços, minúsculo), para comparar sem diferenciar maiúsculas. */
public record Email(String valor) {

    private static final int TAMANHO_MAXIMO = 150;
    private static final Pattern FORMATO = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");

    public Email {
        if (valor == null || valor.isBlank()) {
            throw new CampoObrigatorioException("email");
        }
        String normalizado = valor.strip().toLowerCase(Locale.ROOT);
        if (normalizado.length() > TAMANHO_MAXIMO || !FORMATO.matcher(normalizado).matches()) {
            throw new EmailInvalidoException(valor);
        }
        valor = normalizado;
    }

    public static Email de(String valor) {
        return new Email(valor);
    }

    @Override
    public String toString() {
        return valor;
    }
}
