package br.com.goerp.vendas.aplicacao;

import java.math.RoundingMode;
import java.util.Map;

import br.com.goerp.compartilhado.dominio.Dinheiro;
import br.com.goerp.financeiro.dominio.FormaDePagamento;

/**
 * Resumo de um período (ex.: o dia). Valores só das vendas finalizadas; cancelamentos à parte.
 *
 * @param recebidoPorForma o que entrou por forma (dinheiro já sem troco)
 */
public record ResumoDeVendas(
        long quantidade,
        Dinheiro total,
        Dinheiro descontos,
        Map<FormaDePagamento, Dinheiro> recebidoPorForma,
        long cancelamentos,
        Dinheiro totalCancelado) {

    public Dinheiro ticketMedio() {
        if (quantidade == 0) {
            return Dinheiro.ZERO;
        }
        return Dinheiro.de(total.valor().divide(java.math.BigDecimal.valueOf(quantidade), Dinheiro.CASAS_DECIMAIS,
                RoundingMode.HALF_EVEN));
    }
}
