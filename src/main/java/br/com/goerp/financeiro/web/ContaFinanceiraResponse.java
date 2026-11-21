package br.com.goerp.financeiro.web;

import java.math.BigDecimal;

import br.com.goerp.financeiro.dominio.ContaFinanceira;
import br.com.goerp.financeiro.dominio.TipoDeConta;

public record ContaFinanceiraResponse(Long id, TipoDeConta tipo, String nome, boolean permiteSaldoNegativo,
        BigDecimal saldo, boolean ativa, Long versao) {

    static ContaFinanceiraResponse de(ContaFinanceira conta) {
        return new ContaFinanceiraResponse(conta.id(), conta.tipo(), conta.nome(), conta.permiteSaldoNegativo(),
                conta.saldo().valor(), conta.ativa(), conta.versao());
    }
}
