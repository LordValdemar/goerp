package br.com.goerp.vendas.aplicacao;

import java.time.Instant;

import br.com.goerp.vendas.dominio.SituacaoDaVenda;

/** Todos os campos são opcionais; o período é [de, ate). */
public record FiltroDeVendas(Long caixaId, Instant de, Instant ate, SituacaoDaVenda situacao) {
}
