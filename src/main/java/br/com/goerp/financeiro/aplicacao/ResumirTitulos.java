package br.com.goerp.financeiro.aplicacao;

import java.time.LocalDate;

import br.com.goerp.compartilhado.aplicacao.CasoDeUso;
import br.com.goerp.financeiro.dominio.FiltroDeTitulos;
import br.com.goerp.financeiro.dominio.NaturezaDoTitulo;
import br.com.goerp.financeiro.dominio.TituloRepositorio;

@CasoDeUso
public class ResumirTitulos {

    private final TituloRepositorio titulos;
    private final TitulosDaEmpresa titulosDaEmpresa;

    public ResumirTitulos(TituloRepositorio titulos, TitulosDaEmpresa titulosDaEmpresa) {
        this.titulos = titulos;
        this.titulosDaEmpresa = titulosDaEmpresa;
    }

    public ResumoDeTitulos executar(NaturezaDoTitulo natureza) {
        LocalDate hoje = titulosDaEmpresa.hoje();
        FiltroDeTitulos emAberto = FiltroDeTitulos.emAberto(natureza);
        return new ResumoDeTitulos(titulos.totalizar(emAberto), titulos.totalizar(emAberto.vencidosAntesDe(hoje)),
                titulos.totalizar(emAberto.vencendoEm(hoje)));
    }
}
