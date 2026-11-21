package br.com.goerp.estoque.web;

import java.math.BigDecimal;
import java.time.Instant;

import br.com.goerp.estoque.dominio.Movimentacao;
import br.com.goerp.estoque.dominio.OrigemDaMovimentacao;
import br.com.goerp.estoque.dominio.TipoDeMovimentacao;

public record MovimentacaoResponse(
        Long id,
        Long produtoId,
        TipoDeMovimentacao tipo,
        BigDecimal quantidade,
        BigDecimal efeitoNoSaldo,
        BigDecimal custoUnitario,
        BigDecimal saldoResultante,
        BigDecimal custoMedioResultante,
        OrigemDaMovimentacao origem,
        Long documentoDeOrigem,
        String motivo,
        Long estornada,
        Long usuarioId,
        Instant momento) {

    static MovimentacaoResponse de(Movimentacao m) {
        return new MovimentacaoResponse(m.id(), m.produtoId(), m.tipo(), m.quantidade().valor(), m.efeitoNoSaldo().valor(),
                m.custoUnitario().valor(), m.saldoResultante().valor(), m.custoMedioResultante().valor(), m.origem(),
                m.documentoDeOrigem(), m.motivo(), m.estornada(), m.usuarioId(), m.momento());
    }
}
