package br.com.goerp.financeiro.aplicacao;

import java.util.List;

import br.com.goerp.compartilhado.aplicacao.CasoDeUso;
import br.com.goerp.financeiro.dominio.CategoriaFinanceira;
import br.com.goerp.financeiro.dominio.CategoriaFinanceiraRepositorio;
import br.com.goerp.financeiro.dominio.TipoDeCategoria;

@CasoDeUso
public class ListarCategoriasFinanceiras {

    private final CategoriaFinanceiraRepositorio categorias;

    public ListarCategoriasFinanceiras(CategoriaFinanceiraRepositorio categorias) {
        this.categorias = categorias;
    }

    public List<CategoriaFinanceira> executar(TipoDeCategoria tipo, Boolean ativa) {
        return categorias.listar(tipo, ativa);
    }
}
