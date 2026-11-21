package br.com.goerp.estoque.web;

import java.math.BigDecimal;

import br.com.goerp.estoque.dominio.TipoDeMovimentacao.Sentido;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** Corpos das requisições de estoque. */
final class EstoqueRequests {

    private EstoqueRequests() {
    }

    /** @param custoUnitario opcional: sem ele, a entrada usa o custo médio atual */
    record Entrada(@NotNull Long produtoId, @NotNull BigDecimal quantidade, BigDecimal custoUnitario,
            Long documentoDeOrigem, @Size(max = 500) String observacao) {
    }

    /** Saída manual (consumo interno, doação...): sempre com justificativa. */
    record Saida(@NotNull Long produtoId, @NotNull BigDecimal quantidade, @NotBlank @Size(max = 500) String motivo) {
    }

    record Ajuste(@NotNull Long produtoId, @NotNull Sentido sentido, @NotNull BigDecimal quantidade,
            @NotBlank @Size(max = 500) String motivo) {
    }

    record Estorno(@NotBlank @Size(max = 500) String motivo) {
    }

    record Configuracao(boolean permiteEstoqueNegativo) {
    }
}
