package br.com.goerp.cadastros.produto.aplicacao;

import java.util.List;

import br.com.goerp.cadastros.produto.dominio.Categoria;
import br.com.goerp.cadastros.produto.dominio.CategoriaRepositorio;
import br.com.goerp.compartilhado.aplicacao.CasoDeUso;

@CasoDeUso
public class ListarCategorias {

    private final CategoriaRepositorio categorias;

    public ListarCategorias(CategoriaRepositorio categorias) {
        this.categorias = categorias;
    }

    public List<Categoria> executar(Boolean ativa) {
        return categorias.listar(ativa);
    }
}
