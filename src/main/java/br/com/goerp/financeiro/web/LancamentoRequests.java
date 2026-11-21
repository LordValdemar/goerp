package br.com.goerp.financeiro.web;

import java.math.BigDecimal;

import br.com.goerp.financeiro.dominio.FormaDePagamento;
import br.com.goerp.financeiro.dominio.TipoDeLancamento;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

final class LancamentoRequests {

    private static final int TAMANHO_DA_DESCRICAO = 200;

    private LancamentoRequests() {
    }

    record Avulso(@NotNull Long contaId, @NotNull TipoDeLancamento.Sentido sentido, @NotNull @Positive BigDecimal valor,
            @NotNull FormaDePagamento forma, Long categoriaId,
            @NotBlank @Size(max = TAMANHO_DA_DESCRICAO) String descricao) {
    }

    /** @param forma opcional; padrão TRANSFERENCIA (ex.: DINHEIRO para depósito do cofre no banco) */
    record Transferencia(@NotNull Long contaOrigemId, @NotNull Long contaDestinoId, @NotNull @Positive BigDecimal valor,
            FormaDePagamento forma, @NotBlank @Size(max = TAMANHO_DA_DESCRICAO) String descricao) {
    }

    record Estorno(@NotBlank @Size(max = TAMANHO_DA_DESCRICAO) String motivo) {
    }
}
