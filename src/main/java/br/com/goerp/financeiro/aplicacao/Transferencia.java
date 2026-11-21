package br.com.goerp.financeiro.aplicacao;

import br.com.goerp.financeiro.dominio.Lancamento;

/** As duas pontas de uma transferência, ligadas pelo mesmo número. */
public record Transferencia(long numero, Lancamento saida, Lancamento entrada) {
}
