package br.com.goerp.estoque.aplicacao;

import java.math.BigDecimal;

/** Produto cujo saldo gravado não bate com a soma das movimentações (não deveria acontecer). */
public record DivergenciaDeSaldo(Long produtoId, BigDecimal saldoGravado, BigDecimal somaDasMovimentacoes) {
}
