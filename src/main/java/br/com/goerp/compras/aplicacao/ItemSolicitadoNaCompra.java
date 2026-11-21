package br.com.goerp.compras.aplicacao;

import br.com.goerp.cadastros.produto.dominio.CustoUnitario;
import br.com.goerp.compartilhado.dominio.Quantidade;

/** @param custoNegociado opcional: sem ele, vale o custo do cadastro do produto */
public record ItemSolicitadoNaCompra(Long produtoId, Quantidade quantidade, CustoUnitario custoNegociado) {
}
