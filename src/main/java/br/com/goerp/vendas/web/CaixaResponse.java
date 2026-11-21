package br.com.goerp.vendas.web;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import br.com.goerp.vendas.aplicacao.ResumoDoCaixa;
import br.com.goerp.vendas.dominio.FormaDePagamento;
import br.com.goerp.vendas.dominio.SituacaoDoCaixa;
import br.com.goerp.vendas.dominio.TipoDeMovimentoDeCaixa;

public record CaixaResponse(
        Long id,
        Long operadorId,
        SituacaoDoCaixa situacao,
        BigDecimal trocoInicial,
        Instant abertoEm,
        Instant fechadoEm,
        Long fechadoPor,
        Map<FormaDePagamento, BigDecimal> esperado,
        List<Movimento> movimentos,
        List<Conferencia> conferencias) {

    public record Movimento(TipoDeMovimentoDeCaixa tipo, BigDecimal valor, String motivo, Long usuarioId,
            Instant momento) {
    }

    /** {@code diferenca} positiva é sobra; negativa, falta. */
    public record Conferencia(FormaDePagamento forma, BigDecimal esperado, BigDecimal contado, BigDecimal diferenca) {
    }

    static CaixaResponse de(ResumoDoCaixa resumo) {
        var caixa = resumo.caixa();
        Map<FormaDePagamento, BigDecimal> esperado = new EnumMap<>(FormaDePagamento.class);
        resumo.esperado().forEach((forma, valor) -> esperado.put(forma, valor.valor()));
        return new CaixaResponse(caixa.id(), caixa.operadorId(), caixa.situacao(), caixa.trocoInicial().valor(),
                caixa.abertoEm(), caixa.fechadoEm(), caixa.fechadoPor(), esperado,
                caixa.movimentos().stream()
                        .map(m -> new Movimento(m.tipo(), m.valor().valor(), m.motivo(), m.usuarioId(), m.momento()))
                        .toList(),
                caixa.conferencias().stream()
                        .map(c -> new Conferencia(c.forma(), c.esperado().valor(), c.contado().valor(),
                                c.diferenca().valor()))
                        .toList());
    }
}
