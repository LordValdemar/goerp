package br.com.goerp.cadastros.produto.aplicacao;

import br.com.goerp.cadastros.produto.dominio.Categoria;
import br.com.goerp.cadastros.produto.dominio.CategoriaRepositorio;
import br.com.goerp.compartilhado.aplicacao.CasoDeUso;
import br.com.goerp.compartilhado.dominio.excecao.RecursoNaoEncontradoException;

@CasoDeUso
public class BuscarCategoria {

    private final CategoriaRepositorio categorias;

    public BuscarCategoria(CategoriaRepositorio categorias) {
        this.categorias = categorias;
    }

    public Categoria executar(Long id) {
        return categorias.buscarPorId(id).orElseThrow(() -> new RecursoNaoEncontradoException("Categoria", id));
    }
}
