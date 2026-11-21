package br.com.goerp.financeiro.aplicacao;

import br.com.goerp.compartilhado.aplicacao.CasoDeUso;
import br.com.goerp.financeiro.dominio.NaturezaDoTitulo;

@CasoDeUso
public class BuscarTitulo {

    private final TitulosDaEmpresa titulosDaEmpresa;

    public BuscarTitulo(TitulosDaEmpresa titulosDaEmpresa) {
        this.titulosDaEmpresa = titulosDaEmpresa;
    }

    public TituloConsultado executar(NaturezaDoTitulo natureza, Long id) {
        return titulosDaEmpresa.consultado(titulosDaEmpresa.buscar(natureza, id));
    }
}
