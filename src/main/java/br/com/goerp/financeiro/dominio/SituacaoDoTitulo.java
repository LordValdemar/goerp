package br.com.goerp.financeiro.dominio;

public enum SituacaoDoTitulo {
    ABERTO("aberto"),
    PARCIALMENTE_PAGO("parcialmente pago"),
    QUITADO("quitado"),
    CANCELADO("cancelado");

    private final String descricao;

    SituacaoDoTitulo(String descricao) {
        this.descricao = descricao;
    }

    public String descricao() {
        return descricao;
    }

    /** Ainda há valor a pagar ou receber. */
    public boolean emAberto() {
        return this == ABERTO || this == PARCIALMENTE_PAGO;
    }
}
