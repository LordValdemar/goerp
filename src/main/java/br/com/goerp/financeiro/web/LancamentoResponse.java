package br.com.goerp.financeiro.web;

import java.math.BigDecimal;
import java.time.Instant;

import br.com.goerp.financeiro.dominio.FormaDePagamento;
import br.com.goerp.financeiro.dominio.Lancamento;
import br.com.goerp.financeiro.dominio.OrigemDoLancamento;
import br.com.goerp.financeiro.dominio.TipoDeLancamento;

public record LancamentoResponse(Long id, Long contaId, TipoDeLancamento tipo, BigDecimal valor,
        BigDecimal saldoResultante, FormaDePagamento forma, Long categoriaId, String descricao,
        OrigemDoLancamento origem, Long documentoDeOrigem, Long estornado, Long usuarioId, Instant momento) {

    static LancamentoResponse de(Lancamento l) {
        return new LancamentoResponse(l.id(), l.contaId(), l.tipo(), l.valor().valor(), l.saldoResultante().valor(),
                l.forma(), l.categoriaId(), l.descricao(), l.origem(), l.documentoDeOrigem(), l.estornado(),
                l.usuarioId(), l.momento());
    }
}
