package br.com.goerp.vendas.web;

import java.math.BigDecimal;
import java.util.EnumMap;
import java.util.Map;

import br.com.goerp.vendas.aplicacao.ResumoDeVendas;
import br.com.goerp.vendas.dominio.FormaDePagamento;

public record ResumoDeVendasResponse(
        long quantidade,
        BigDecimal total,
        BigDecimal descontos,
        BigDecimal ticketMedio,
        Map<FormaDePagamento, BigDecimal> recebidoPorForma,
        long cancelamentos,
        BigDecimal totalCancelado) {

    static ResumoDeVendasResponse de(ResumoDeVendas resumo) {
        Map<FormaDePagamento, BigDecimal> recebido = new EnumMap<>(FormaDePagamento.class);
        resumo.recebidoPorForma().forEach((forma, valor) -> recebido.put(forma, valor.valor()));
        return new ResumoDeVendasResponse(resumo.quantidade(), resumo.total().valor(), resumo.descontos().valor(),
                resumo.ticketMedio().valor(), recebido, resumo.cancelamentos(), resumo.totalCancelado().valor());
    }
}
