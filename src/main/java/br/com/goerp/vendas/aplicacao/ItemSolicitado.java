package br.com.goerp.vendas.aplicacao;

import br.com.goerp.compartilhado.dominio.Dinheiro;
import br.com.goerp.compartilhado.dominio.Quantidade;

/** Item pedido pelo usuário: o produto vem pelo id ou pelo código de barras; o preço vem do cadastro. */
public record ItemSolicitado(Long produtoId, String codigoDeBarras, Quantidade quantidade, Dinheiro desconto) {
}
