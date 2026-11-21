package br.com.goerp.usuario.web;

import jakarta.validation.constraints.NotNull;

public record TrocaDePerfilRequest(@NotNull Long perfilId) {
}
