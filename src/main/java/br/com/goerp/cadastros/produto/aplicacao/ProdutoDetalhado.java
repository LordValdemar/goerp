package br.com.goerp.cadastros.produto.aplicacao;

import br.com.goerp.cadastros.produto.dominio.Categoria;
import br.com.goerp.cadastros.produto.dominio.Produto;
import br.com.goerp.cadastros.produto.dominio.UnidadeDeMedida;

/** Produto com a unidade e a categoria (que pode ser nula), para exibição. */
public record ProdutoDetalhado(Produto produto, UnidadeDeMedida unidade, Categoria categoria) {
}
