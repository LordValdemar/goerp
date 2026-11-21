package br.com.goerp.compras.aplicacao;

import br.com.goerp.cadastros.produto.dominio.CustoUnitario;
import br.com.goerp.compartilhado.dominio.Quantidade;

/** @param custoUnitario o custo da nota; sem ele, vale o custo negociado no pedido */
public record ItemInformadoNoRecebimento(Long produtoId, Quantidade quantidade, CustoUnitario custoUnitario) {
}
