package br.com.goerp.cadastros.produto.web;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** @param sigla só no cadastro (depois não muda) */
public record UnidadeDeMedidaRequest(String sigla, @NotBlank @Size(max = 60) String descricao, boolean fracionavel) {
}
