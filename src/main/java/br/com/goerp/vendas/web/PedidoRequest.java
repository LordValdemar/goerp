package br.com.goerp.vendas.web;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import br.com.goerp.compartilhado.dominio.Dinheiro;
import br.com.goerp.compartilhado.dominio.Quantidade;
import br.com.goerp.vendas.aplicacao.ItemSolicitado;
import br.com.goerp.vendas.aplicacao.pedido.DadosDoPedido;
import br.com.goerp.vendas.web.VendaRequest.Item;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * @param clienteId só na criação
 * @param versao    só na alteração (a versão lida)
 */
public record PedidoRequest(
        Long clienteId,
        @NotEmpty List<@Valid Item> itens,
        BigDecimal desconto,
        @NotNull LocalDate validade,
        @Size(max = 1000) String observacoes,
        Long versao) {

    DadosDoPedido paraDados() {
        return new DadosDoPedido(
                itens.stream().map(item -> new ItemSolicitado(item.produtoId(), item.codigoDeBarras(),
                        new Quantidade(item.quantidade()), item.desconto() == null ? null : Dinheiro.de(item.desconto())))
                        .toList(),
                desconto == null ? null : Dinheiro.de(desconto), validade, observacoes);
    }
}
