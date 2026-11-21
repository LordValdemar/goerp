package br.com.goerp.financeiro.web;

import br.com.goerp.financeiro.dominio.TipoDeConta;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

final class ContaFinanceiraRequests {

    private ContaFinanceiraRequests() {
    }

    record Cadastro(@NotNull TipoDeConta tipo, @NotBlank @Size(max = 80) String nome, boolean permiteSaldoNegativo) {
    }

    /** @param versao a versão que foi lida, para detectar edição simultânea */
    record Alteracao(@NotBlank @Size(max = 80) String nome, boolean permiteSaldoNegativo, @NotNull Long versao) {
    }
}
