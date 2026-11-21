package br.com.goerp.financeiro.dominio;

import java.time.Instant;

/** Filtros opcionais do extrato: forma de pagamento e período [de, ate). */
public record FiltroDeExtrato(FormaDePagamento forma, Instant de, Instant ate) {

    public static FiltroDeExtrato tudo() {
        return new FiltroDeExtrato(null, null, null);
    }
}
