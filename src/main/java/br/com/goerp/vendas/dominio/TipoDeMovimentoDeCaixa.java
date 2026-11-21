package br.com.goerp.vendas.dominio;

/** Entrada ou retirada de dinheiro que não é venda. */
public enum TipoDeMovimentoDeCaixa {
    /** Reforço de troco colocado no caixa. */
    SUPRIMENTO,
    /** Retirada de dinheiro do caixa (ex.: levar ao cofre), sempre com motivo. */
    SANGRIA
}
