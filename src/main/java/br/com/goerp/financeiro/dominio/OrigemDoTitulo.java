package br.com.goerp.financeiro.dominio;

/** Quem gerou o título; com o documento de origem, leva ao recebimento de compra ou à venda. */
public enum OrigemDoTitulo {
    MANUAL,
    RECEBIMENTO_DE_COMPRA,
    VENDA,
    PEDIDO_DE_VENDA
}
