package br.com.goerp.estoque.aplicacao;

import java.util.List;

import br.com.goerp.compartilhado.dominio.Pagina;
import br.com.goerp.compartilhado.dominio.Paginacao;

/**
 * Porta de leitura: relatórios que cruzam produtos e saldos numa única consulta ao banco,
 * em vez de carregar cada produto e cada saldo separadamente.
 */
public interface ConsultasDeEstoque {

    /** Produtos ativos que controlam estoque, com saldo (zero se nunca movimentado). */
    Pagina<PosicaoDeEstoque> posicoes(FiltroDePosicoes filtro, Paginacao paginacao);

    List<DivergenciaDeSaldo> divergencias();
}
