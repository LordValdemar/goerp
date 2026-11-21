package br.com.goerp.vendas.dominio;

/** ORCAMENTO -> CONFIRMADO -> FATURADO; cancelável antes de faturar. */
public enum SituacaoDoPedido {
    ORCAMENTO,
    CONFIRMADO,
    FATURADO,
    CANCELADO
}
