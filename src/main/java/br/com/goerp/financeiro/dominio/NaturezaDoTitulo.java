package br.com.goerp.financeiro.dominio;

public enum NaturezaDoTitulo {
    /** Conta a pagar: a baixa tira dinheiro da conta. */
    A_PAGAR(TipoDeLancamento.Sentido.SAIDA),
    /** Conta a receber: a baixa põe dinheiro na conta. */
    A_RECEBER(TipoDeLancamento.Sentido.ENTRADA);

    private final TipoDeLancamento.Sentido sentidoDaBaixa;

    NaturezaDoTitulo(TipoDeLancamento.Sentido sentidoDaBaixa) {
        this.sentidoDaBaixa = sentidoDaBaixa;
    }

    public TipoDeLancamento.Sentido sentidoDaBaixa() {
        return sentidoDaBaixa;
    }
}
