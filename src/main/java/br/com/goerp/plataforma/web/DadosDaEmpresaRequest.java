package br.com.goerp.plataforma.web;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record DadosDaEmpresaRequest(@NotBlank @Size(max = 150) String razaoSocial, @Size(max = 150) String nomeFantasia) {
}
