package br.com.goerp.usuario.dominio;

import br.com.goerp.compartilhado.dominio.excecao.CampoObrigatorioException;

/**
 * Senha em texto puro, digitada pelo usuário, que atende à política de senhas.
 * Existe só em memória: nunca é persistida nem aparece em logs.
 */
public record Senha(String valor) {

    public static final int TAMANHO_MINIMO = 8;
    /** Limite do BCrypt: bytes além de 72 são ignorados. */
    public static final int TAMANHO_MAXIMO = 72;

    public Senha {
        if (valor == null || valor.isEmpty()) {
            throw new CampoObrigatorioException("senha");
        }
        if (!atendePolitica(valor)) {
            throw new SenhaFracaException();
        }
    }

    public static Senha de(String valor) {
        return new Senha(valor);
    }

    private static boolean atendePolitica(String valor) {
        boolean temLetra = valor.chars().anyMatch(Character::isLetter);
        boolean temNumero = valor.chars().anyMatch(Character::isDigit);
        return valor.length() >= TAMANHO_MINIMO && valor.length() <= TAMANHO_MAXIMO && temLetra && temNumero;
    }

    @Override
    public String toString() {
        return "********";
    }
}
