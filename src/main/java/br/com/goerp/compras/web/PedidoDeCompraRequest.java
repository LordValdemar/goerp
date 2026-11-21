package br.com.goerp.compras.web;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import br.com.goerp.cadastros.produto.dominio.CustoUnitario;
import br.com.goerp.compartilhado.dominio.Quantidade;
import br.com.goerp.compras.aplicacao.DadosDoPedidoDeCompra;
import br.com.goerp.compras.aplicacao.ItemSolicitadoNaCompra;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * @param fornecedorId só na criação
 * @param versao       só na alteração (a versão lida)
 */
public record PedidoDeCompraRequest(
        Long fornecedorId,
        @NotEmpty List<@Valid Item> itens,
        LocalDate previsaoDeEntrega,
        @Size(max = 1000) String observacoes,
        Long versao) {

    /** @param custoNegociado opcional: sem ele, vale o custo do cadastro */
    public record Item(@NotNull Long produtoId, @NotNull BigDecimal quantidade, BigDecimal custoNegociado) {
    }

    DadosDoPedidoDeCompra paraDados() {
        return new DadosDoPedidoDeCompra(itens.stream().map(item -> new ItemSolicitadoNaCompra(item.produtoId(),
                new Quantidade(item.quantidade()),
                item.custoNegociado() == null ? null : new CustoUnitario(item.custoNegociado()))).toList(),
                previsaoDeEntrega, observacoes);
    }
}
