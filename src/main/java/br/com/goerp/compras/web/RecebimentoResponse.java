package br.com.goerp.compras.web;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import br.com.goerp.compras.dominio.Recebimento;

public record RecebimentoResponse(
        Long id,
        Long pedidoId,
        String notaFiscal,
        List<Item> itens,
        BigDecimal valorTotal,
        Long recebidoPor,
        Instant recebidoEm,
        boolean estornado,
        Instant estornadoEm,
        String motivoDoEstorno) {

    public record Item(Long produtoId, BigDecimal quantidade, BigDecimal custoUnitario, BigDecimal valor) {
    }

    static RecebimentoResponse de(Recebimento r) {
        return new RecebimentoResponse(r.id(), r.pedidoId(), r.notaFiscal(),
                r.itens().stream().map(i -> new Item(i.produtoId(), i.quantidade().valor(), i.custoUnitario().valor(),
                        i.valor().valor())).toList(),
                r.valorTotal().valor(), r.recebidoPor(), r.recebidoEm(), r.estornado(), r.estornadoEm(),
                r.motivoDoEstorno());
    }
}
