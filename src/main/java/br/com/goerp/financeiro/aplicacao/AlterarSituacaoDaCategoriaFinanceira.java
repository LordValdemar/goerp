package br.com.goerp.financeiro.aplicacao;

import br.com.goerp.compartilhado.aplicacao.CasoDeUso;
import br.com.goerp.financeiro.dominio.CategoriaFinanceira;
import br.com.goerp.financeiro.dominio.CategoriaFinanceiraRepositorio;

@CasoDeUso
public class AlterarSituacaoDaCategoriaFinanceira {

    private final CategoriaFinanceiraRepositorio categorias;
    private final BuscarCategoriaFinanceira buscarCategoria;

    public AlterarSituacaoDaCategoriaFinanceira(CategoriaFinanceiraRepositorio categorias,
            BuscarCategoriaFinanceira buscarCategoria) {
        this.categorias = categorias;
        this.buscarCategoria = buscarCategoria;
    }

    public CategoriaFinanceira executar(Long id, boolean ativar) {
        CategoriaFinanceira categoria = buscarCategoria.executar(id);
        categoria.definirSituacao(ativar);
        return categorias.salvar(categoria);
    }
}
