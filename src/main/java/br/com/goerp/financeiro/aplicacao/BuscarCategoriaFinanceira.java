package br.com.goerp.financeiro.aplicacao;

import br.com.goerp.compartilhado.aplicacao.CasoDeUso;
import br.com.goerp.compartilhado.dominio.excecao.RecursoNaoEncontradoException;
import br.com.goerp.financeiro.dominio.CategoriaFinanceira;
import br.com.goerp.financeiro.dominio.CategoriaFinanceiraRepositorio;

@CasoDeUso
public class BuscarCategoriaFinanceira {

    private final CategoriaFinanceiraRepositorio categorias;

    public BuscarCategoriaFinanceira(CategoriaFinanceiraRepositorio categorias) {
        this.categorias = categorias;
    }

    public CategoriaFinanceira executar(Long id) {
        return categorias.buscarPorId(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Categoria financeira", id));
    }
}
