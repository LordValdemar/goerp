package br.com.goerp.financeiro.dominio;

/** O que aconteceu com o saldo da conta. O sentido diz se o valor soma ou subtrai. */
public enum TipoDeLancamento {
    ENTRADA(Sentido.ENTRADA),
    SAIDA(Sentido.SAIDA),
    ESTORNO_DE_ENTRADA(Sentido.SAIDA),
    ESTORNO_DE_SAIDA(Sentido.ENTRADA);

    public enum Sentido {
        ENTRADA,
        SAIDA
    }

    private final Sentido sentido;

    TipoDeLancamento(Sentido sentido) {
        this.sentido = sentido;
    }

    public Sentido sentido() {
        return sentido;
    }

    public boolean soma() {
        return sentido == Sentido.ENTRADA;
    }

    public boolean ehEstorno() {
        return this == ESTORNO_DE_ENTRADA || this == ESTORNO_DE_SAIDA;
    }

    public TipoDeLancamento estorno() {
        if (ehEstorno()) {
            throw new EstornoDeEstornoException();
        }
        return soma() ? ESTORNO_DE_ENTRADA : ESTORNO_DE_SAIDA;
    }

    public static TipoDeLancamento normal(Sentido sentido) {
        return sentido == Sentido.ENTRADA ? ENTRADA : SAIDA;
    }
}
