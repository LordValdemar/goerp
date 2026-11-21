package br.com.goerp.estoque.web;

import java.math.BigDecimal;

import br.com.goerp.estoque.dominio.SaldoDeEstoque;

public record SaldoResponse(Long produtoId, BigDecimal quantidade, BigDecimal custoMedio) {

    static SaldoResponse de(SaldoDeEstoque saldo) {
        return new SaldoResponse(saldo.produtoId(), saldo.quantidade().valor(), saldo.custoMedio().valor());
    }
}
