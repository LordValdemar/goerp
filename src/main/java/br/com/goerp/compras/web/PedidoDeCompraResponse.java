package br.com.goerp.compras.web;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import br.com.goerp.compras.aplicacao.ConsultarPedidosDeCompra.PedidoComPrazo;
import br.com.goerp.compras.dominio.PedidoDeCompra;
import br.com.goerp.compras.dominio.SituacaoDoPedidoDeCompra;

public record PedidoDeCompraResponse(
        Long id,
        Long numero,
        Long fornecedorId,
        Long compradorId,
        SituacaoDoPedidoDeCompra situacao,
        boolean atrasado,
        List<Item> itens,
        BigDecimal total,
        LocalDate previsaoDeEntrega,
        String observacoes,
        Instant criadoEm,
        Instant enviadoEm,
        Instant encerradoEm,
        String motivoDoEncerramento,
        Long versao) {

    public record Item(Long produtoId, String codigo, String descricao, BigDecimal quantidade, BigDecimal custoNegociado,
            BigDecimal valor, BigDecimal recebida, BigDecimal pendente) {
    }

    static PedidoDeCompraResponse de(PedidoComPrazo comPrazo) {
        return de(comPrazo.pedido(), comPrazo.atrasado());
    }

    static PedidoDeCompraResponse de(PedidoDeCompra pedido) {
        return de(pedido, false);
    }

    private static PedidoDeCompraResponse de(PedidoDeCompra pedido, boolean atrasado) {
        var conteudo = pedido.conteudo();
        var andamento = pedido.andamento();
        return new PedidoDeCompraResponse(pedido.id(), pedido.numero(), pedido.fornecedorId(), pedido.compradorId(),
                andamento.situacao(), atrasado,
                pedido.itens().stream().map(i -> new Item(i.produtoId(), i.codigo(), i.descricao(), i.quantidade().valor(),
                        i.custoNegociado().valor(), i.valor().valor(), i.recebida().valor(), i.pendente().valor()))
                        .toList(),
                pedido.total().valor(), conteudo.previsaoDeEntrega(), conteudo.observacoes(), pedido.criadoEm(),
                andamento.enviadoEm(), andamento.encerradoEm(), andamento.motivoDoEncerramento(), pedido.versao());
    }
}
