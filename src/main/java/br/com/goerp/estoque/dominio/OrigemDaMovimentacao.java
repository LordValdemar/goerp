package br.com.goerp.estoque.dominio;

/** De onde veio a movimentação: lançamento manual ou outro módulo. */
public enum OrigemDaMovimentacao {
    MANUAL,
    COMPRA,
    VENDA,
    PEDIDO_DE_VENDA,
    INVENTARIO
}
