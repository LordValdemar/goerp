package br.com.goerp.compartilhado.dominio;

import br.com.goerp.compartilhado.dominio.excecao.CampoObrigatorioException;

/** Validações de texto reutilizadas pelas entidades de domínio. */
public final class Textos {

    private Textos() {
    }

    public static String obrigatorio(String valor, String campo) {
        if (valor == null || valor.isBlank()) {
            throw new CampoObrigatorioException(campo);
        }
        return valor.strip();
    }

    public static String opcional(String valor) {
        return valor == null || valor.isBlank() ? null : valor.strip();
    }
}
