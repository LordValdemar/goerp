package br.com.goerp.usuario.dominio;

import java.util.Objects;

/** Hash da senha, a única forma em que a senha é guardada. */
public record SenhaCriptografada(String hash) {

    public SenhaCriptografada {
        Objects.requireNonNull(hash, "hash");
    }

    @Override
    public String toString() {
        return "********";
    }
}
