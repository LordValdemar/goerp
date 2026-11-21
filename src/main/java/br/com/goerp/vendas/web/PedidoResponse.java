package br.com.goerp.vendas.web;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import br.com.goerp.vendas.dominio.PedidoDeVenda;
import br.com.goerp.vendas.dominio.SituacaoDoPedido;

public record PedidoResponse(
        Long id,
        Long numero,
        Long clienteId,
        Long vendedorId,
        SituacaoDoPedido situacao,
        List<VendaResponse.Item> itens,
        BigDecimal bruto,
        BigDecimal desconto,
        BigDecimal total,
        LocalDate validade,
        String observacoes,
        Instant criadoEm,
        Instant confirmadoEm,
        Instant faturadoEm,
        Instant canceladoEm,
        String motivoDoCancelamento,
        Long versao) {

    static PedidoResponse de(PedidoDeVenda pedido) {
        var conteudo = pedido.conteudo();
        var andamento = pedido.andamento();
        return new PedidoResponse(pedido.id(), pedido.numero(), pedido.clienteId(), pedido.vendedorId(),
                andamento.situacao(),
                pedido.itens().stream().map(i -> new VendaResponse.Item(i.produtoId(), i.codigo(), i.descricao(),
                        i.quantidade().valor(), i.precoUnitario().valor(), i.desconto().valor(), i.total().valor())).toList(),
                pedido.bruto().valor(), pedido.descontoTotal().valor(), pedido.total().valor(), conteudo.validade(),
                conteudo.observacoes(), pedido.criadoEm(), andamento.confirmadoEm(), andamento.faturadoEm(),
                andamento.canceladoEm(), andamento.motivoDoCancelamento(), pedido.versao());
    }
}
