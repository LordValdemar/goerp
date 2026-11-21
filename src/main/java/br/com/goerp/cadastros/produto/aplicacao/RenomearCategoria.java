package br.com.goerp.cadastros.produto.aplicacao;

import br.com.goerp.cadastros.produto.dominio.Categoria;
import br.com.goerp.cadastros.produto.dominio.CategoriaJaCadastradaException;
import br.com.goerp.cadastros.produto.dominio.CategoriaRepositorio;
import br.com.goerp.compartilhado.aplicacao.CasoDeUso;

@CasoDeUso
public class RenomearCategoria {

    private final CategoriaRepositorio categorias;
    private final BuscarCategoria buscar;

    public RenomearCategoria(CategoriaRepositorio categorias, BuscarCategoria buscar) {
        this.categorias = categorias;
        this.buscar = buscar;
    }

    public Categoria executar(Long id, String nome) {
        Categoria categoria = buscar.executar(id);
        categoria.renomear(nome);
        if (categorias.existeOutraComNome(categoria.nome(), id)) {
            throw new CategoriaJaCadastradaException(categoria.nome());
        }
        return categorias.salvar(categoria);
    }
}
