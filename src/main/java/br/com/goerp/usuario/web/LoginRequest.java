package br.com.goerp.usuario.web;

import jakarta.validation.constraints.NotBlank;

public record LoginRequest(@NotBlank String email, @NotBlank String senha, Long empresaId) {

    @Override
    public String toString() {
        return "LoginRequest[email=" + email + ", empresaId=" + empresaId + "]";
    }
}
