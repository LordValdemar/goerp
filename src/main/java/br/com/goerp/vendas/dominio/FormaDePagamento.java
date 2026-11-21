package br.com.goerp.vendas.dominio;

/**
 * Formas aceitas no PDV. Só o dinheiro gera troco. (A prazo entra com o módulo financeiro.)
 */
public enum FormaDePagamento {
    DINHEIRO,
    CARTAO_DEBITO,
    CARTAO_CREDITO,
    PIX;

    public boolean geraTroco() {
        return this == DINHEIRO;
    }
}
