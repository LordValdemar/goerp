package br.com.goerp.financeiro.aplicacao;

import br.com.goerp.compartilhado.aplicacao.CasoDeUso;
import br.com.goerp.compartilhado.dominio.Pagina;
import br.com.goerp.compartilhado.dominio.Paginacao;
import br.com.goerp.financeiro.dominio.FiltroDeTitulos;
import br.com.goerp.financeiro.dominio.TituloRepositorio;

/** Títulos por vencimento (mais antigos primeiro); {@code somenteVencidos} usa a data de hoje da empresa. */
@CasoDeUso
public class PesquisarTitulos {

    private final TituloRepositorio titulos;
    private final TitulosDaEmpresa titulosDaEmpresa;

    public PesquisarTitulos(TituloRepositorio titulos, TitulosDaEmpresa titulosDaEmpresa) {
        this.titulos = titulos;
        this.titulosDaEmpresa = titulosDaEmpresa;
    }

    public Pagina<TituloConsultado> executar(FiltroDeTitulos filtro, boolean somenteVencidos, Paginacao paginacao) {
        FiltroDeTitulos aplicado = somenteVencidos ? filtro.vencidosAntesDe(titulosDaEmpresa.hoje()) : filtro;
        return titulos.pesquisar(aplicado, paginacao).mapear(titulosDaEmpresa::consultado);
    }
}
