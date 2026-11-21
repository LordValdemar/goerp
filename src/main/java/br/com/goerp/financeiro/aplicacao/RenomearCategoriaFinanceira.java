package br.com.goerp.financeiro.aplicacao;

import br.com.goerp.compartilhado.aplicacao.CasoDeUso;
import br.com.goerp.financeiro.dominio.CategoriaFinanceira;
import br.com.goerp.financeiro.dominio.CategoriaFinanceiraJaCadastradaException;
import br.com.goerp.financeiro.dominio.CategoriaFinanceiraRepositorio;

/** O tipo (receita/despesa) não muda: os lançamentos já classificados dependem dele. */
@CasoDeUso
public class RenomearCategoriaFinanceira {

    private final CategoriaFinanceiraRepositorio categorias;
    private final BuscarCategoriaFinanceira buscarCategoria;

    public RenomearCategoriaFinanceira(CategoriaFinanceiraRepositorio categorias,
            BuscarCategoriaFinanceira buscarCategoria) {
        this.categorias = categorias;
        this.buscarCategoria = buscarCategoria;
    }

    public CategoriaFinanceira executar(Long id, String nome) {
        CategoriaFinanceira categoria = buscarCategoria.executar(id);
        categoria.renomear(nome);
        if (categorias.existeOutraComNome(categoria.nome(), id)) {
            throw new CategoriaFinanceiraJaCadastradaException(categoria.nome());
        }
        return categorias.salvar(categoria);
    }
}
