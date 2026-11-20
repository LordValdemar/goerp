package br.com.goerp.usuario.web;

import jakarta.validation.constraints.NotBlank;

public record TrocaDeSenhaRequest(@NotBlank String senhaAtual, @NotBlank String novaSenha) {

    @Override
    public String toString() {
        return "TrocaDeSenhaRequest[********]";
    }
}
