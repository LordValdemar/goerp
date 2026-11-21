package br.com.goerp.vendas.web;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import br.com.goerp.vendas.dominio.FormaDePagamento;
import br.com.goerp.vendas.dominio.SituacaoDaVenda;
import br.com.goerp.vendas.dominio.Venda;

public record VendaResponse(
        Long id,
        Long numero,
        Long caixaId,
        Long operadorId,
        Long clienteId,
        String cpfNaNota,
        List<Item> itens,
        BigDecimal bruto,
        BigDecimal desconto,
        BigDecimal total,
        List<Pagamento> pagamentos,
        BigDecimal troco,
        SituacaoDaVenda situacao,
        Instant momento,
        Instant canceladaEm,
        String motivoDoCancelamento) {

    public record Item(Long produtoId, String codigo, String descricao, BigDecimal quantidade, BigDecimal precoUnitario,
            BigDecimal desconto, BigDecimal total) {
    }

    public record Pagamento(FormaDePagamento forma, BigDecimal valor) {
    }

    static VendaResponse de(Venda venda) {
        return new VendaResponse(venda.id(), venda.numero(), venda.caixaId(), venda.operadorId(), venda.clienteId(),
                venda.cpfNaNota() == null ? null : venda.cpfNaNota().formatado(),
                venda.itens().stream().map(i -> new Item(i.produtoId(), i.codigo(), i.descricao(), i.quantidade().valor(),
                        i.precoUnitario().valor(), i.desconto().valor(), i.total().valor())).toList(),
                venda.bruto().valor(), venda.descontoTotal().valor(), venda.total().valor(),
                venda.pagamentos().stream().map(p -> new Pagamento(p.forma(), p.valor().valor())).toList(),
                venda.troco().valor(), venda.situacao(), venda.momento(), venda.canceladaEm(),
                venda.motivoDoCancelamento());
    }
}
