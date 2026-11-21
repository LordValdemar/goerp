package br.com.goerp.compras.dominio;

/**
 * RASCUNHO -> ENVIADO -> PARCIALMENTE_RECEBIDO -> RECEBIDO.
 * Sem nada recebido, pode ser CANCELADO; com recebimento parcial, o saldo pode ser ENCERRADO.
 */
public enum SituacaoDoPedidoDeCompra {
    RASCUNHO("em rascunho"),
    ENVIADO("enviado"),
    PARCIALMENTE_RECEBIDO("parcialmente recebido"),
    RECEBIDO("recebido"),
    CANCELADO("cancelado"),
    ENCERRADO("encerrado");

    private final String descricao;

    SituacaoDoPedidoDeCompra(String descricao) {
        this.descricao = descricao;
    }

    public String descricao() {
        return descricao;
    }

    public boolean aguardandoEntrega() {
        return this == ENVIADO || this == PARCIALMENTE_RECEBIDO;
    }
}
