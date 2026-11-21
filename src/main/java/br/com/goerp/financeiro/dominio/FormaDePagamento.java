package br.com.goerp.financeiro.dominio;

import java.util.List;

/**
 * Como o dinheiro foi (ou será) pago ou recebido. Fica marcada em todo lançamento, baixa e título,
 * para saber de onde veio e para onde foi cada valor.
 */
public enum FormaDePagamento {
    DINHEIRO,
    PIX,
    CARTAO_DEBITO,
    CARTAO_CREDITO,
    /** Só para pagar contas e fornecedores. */
    BOLETO,
    /** Só para pagar contas e fornecedores. */
    TRANSFERENCIA,
    /** Fica devendo: gera título, não lançamento. Quando for pago, a baixa diz a forma usada. */
    A_PRAZO;

    private static final List<FormaDePagamento> CONFERIDAS_NO_CAIXA = List.of(DINHEIRO, PIX, CARTAO_DEBITO,
            CARTAO_CREDITO);

    /** Formas que entram no caixa do PDV e são conferidas no fechamento. */
    public static List<FormaDePagamento> conferidasNoCaixa() {
        return CONFERIDAS_NO_CAIXA;
    }

    public boolean conferidaNoCaixa() {
        return CONFERIDAS_NO_CAIXA.contains(this);
    }

    public boolean geraTroco() {
        return this == DINHEIRO;
    }

    /** Se o dinheiro muda de mãos agora (e por isso gera lançamento numa conta financeira). */
    public boolean movimentaNaHora() {
        return this != A_PRAZO;
    }
}
