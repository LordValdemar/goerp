package br.com.goerp.financeiro.web;

import br.com.goerp.financeiro.dominio.TipoDeCategoria;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

final class CategoriaFinanceiraRequests {

    private CategoriaFinanceiraRequests() {
    }

    record Cadastro(@NotNull TipoDeCategoria tipo, @NotBlank @Size(max = 80) String nome) {
    }

    record Renomeacao(@NotBlank @Size(max = 80) String nome) {
    }
}
