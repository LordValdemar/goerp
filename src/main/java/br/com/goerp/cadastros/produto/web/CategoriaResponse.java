package br.com.goerp.cadastros.produto.web;

import br.com.goerp.cadastros.produto.dominio.Categoria;

public record CategoriaResponse(Long id, String nome, boolean ativa) {

    static CategoriaResponse de(Categoria categoria) {
        return new CategoriaResponse(categoria.id(), categoria.nome(), categoria.ativa());
    }
}
