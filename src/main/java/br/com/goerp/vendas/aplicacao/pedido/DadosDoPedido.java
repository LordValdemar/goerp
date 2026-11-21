package br.com.goerp.vendas.aplicacao.pedido;

import java.time.LocalDate;
import java.util.List;

import br.com.goerp.compartilhado.dominio.Dinheiro;
import br.com.goerp.vendas.aplicacao.ItemSolicitado;

public record DadosDoPedido(List<ItemSolicitado> itens, Dinheiro desconto, LocalDate validade, String observacoes) {
}
