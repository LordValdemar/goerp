package br.com.goerp.vendas.aplicacao;

import java.util.Map;

import br.com.goerp.compartilhado.dominio.Dinheiro;
import br.com.goerp.vendas.dominio.Caixa;
import br.com.goerp.vendas.dominio.FormaDePagamento;

/** O caixa e quanto se espera encontrar nele em cada forma de pagamento agora. */
public record ResumoDoCaixa(Caixa caixa, Map<FormaDePagamento, Dinheiro> esperado) {
}
