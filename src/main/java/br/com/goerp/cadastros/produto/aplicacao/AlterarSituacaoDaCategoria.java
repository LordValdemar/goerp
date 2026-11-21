package br.com.goerp.cadastros.produto.aplicacao;

import br.com.goerp.cadastros.produto.dominio.Categoria;
import br.com.goerp.cadastros.produto.dominio.CategoriaRepositorio;
import br.com.goerp.compartilhado.aplicacao.CasoDeUso;

/** Inativar impede o uso em novos produtos; os que já estão na categoria continuam nela. */
@CasoDeUso
public class AlterarSituacaoDaCategoria {

    private final CategoriaRepositorio categorias;
    private final BuscarCategoria buscar;

    public AlterarSituacaoDaCategoria(CategoriaRepositorio categorias, BuscarCategoria buscar) {
        this.categorias = categorias;
        this.buscar = buscar;
    }

    public Categoria executar(Long id, boolean ativar) {
        Categoria categoria = buscar.executar(id);
        categoria.definirSituacao(ativar);
        return categorias.salvar(categoria);
    }
}
