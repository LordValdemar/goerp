package br.com.goerp.vendas.aplicacao;

import br.com.goerp.compartilhado.dominio.Pagina;
import br.com.goerp.compartilhado.dominio.Paginacao;
import br.com.goerp.vendas.dominio.Venda;

/** Porta de leitura das vendas (filtradas por empresa automaticamente). */
public interface ConsultasDeVendas {

    /** Da mais recente para a mais antiga. */
    Pagina<Venda> pesquisar(FiltroDeVendas filtro, Paginacao paginacao);

    ResumoDeVendas resumir(FiltroDeVendas filtro);
}
