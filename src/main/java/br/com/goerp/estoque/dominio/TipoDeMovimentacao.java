package br.com.goerp.estoque.dominio;

/** O que aconteceu com o estoque. O sentido diz se a quantidade soma ou subtrai do saldo. */
public enum TipoDeMovimentacao {
    ENTRADA(Sentido.ENTRADA),
    SAIDA(Sentido.SAIDA),
    AJUSTE_DE_ENTRADA(Sentido.ENTRADA),
    AJUSTE_DE_SAIDA(Sentido.SAIDA),
    ESTORNO_DE_ENTRADA(Sentido.SAIDA),
    ESTORNO_DE_SAIDA(Sentido.ENTRADA);

    public enum Sentido {
        ENTRADA,
        SAIDA
    }

    private final Sentido sentido;

    TipoDeMovimentacao(Sentido sentido) {
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

    /** Ajustes e estornos mexem no saldo "por fora" do fluxo normal: precisam de justificativa. */
    public boolean exigeMotivo() {
        return this != ENTRADA && this != SAIDA;
    }

    /** O estorno de uma movimentação tem o sentido oposto ao dela. */
    public TipoDeMovimentacao estorno() {
        if (ehEstorno()) {
            throw new EstornoDeEstornoException();
        }
        return soma() ? ESTORNO_DE_ENTRADA : ESTORNO_DE_SAIDA;
    }
}
