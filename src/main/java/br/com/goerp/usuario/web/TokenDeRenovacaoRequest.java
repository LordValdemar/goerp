package br.com.goerp.usuario.web;

import jakarta.validation.constraints.NotBlank;

public record TokenDeRenovacaoRequest(@NotBlank String tokenDeRenovacao) {
}
