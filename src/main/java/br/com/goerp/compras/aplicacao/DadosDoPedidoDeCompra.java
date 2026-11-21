package br.com.goerp.compras.aplicacao;

import java.time.LocalDate;
import java.util.List;

public record DadosDoPedidoDeCompra(List<ItemSolicitadoNaCompra> itens, LocalDate previsaoDeEntrega,
        String observacoes) {
}
