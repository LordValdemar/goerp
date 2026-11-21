package br.com.goerp.financeiro.dominio;

import br.com.goerp.compartilhado.dominio.Dinheiro;

/** Quantos títulos e quanto ainda devem (saldo devedor somado). */
public record TotalDeTitulos(long quantidade, Dinheiro saldoDevedor) {
}
