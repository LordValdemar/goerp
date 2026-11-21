package br.com.goerp.vendas.aplicacao;

import br.com.goerp.compartilhado.aplicacao.CasoDeUso;
import br.com.goerp.compartilhado.dominio.Pagina;
import br.com.goerp.compartilhado.dominio.Paginacao;
import br.com.goerp.vendas.dominio.Venda;

@CasoDeUso
public class ConsultarVendas {

    private final ConsultasDeVendas consultas;

    public ConsultarVendas(ConsultasDeVendas consultas) {
        this.consultas = consultas;
    }

    public Pagina<Venda> pesquisar(FiltroDeVendas filtro, Paginacao paginacao) {
        return consultas.pesquisar(filtro, paginacao);
    }

    public ResumoDeVendas resumir(FiltroDeVendas filtro) {
        return consultas.resumir(filtro);
    }
}
