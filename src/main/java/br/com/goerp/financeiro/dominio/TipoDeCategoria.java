package br.com.goerp.financeiro.dominio;

public enum TipoDeCategoria {
    RECEITA,
    DESPESA;

    public boolean aceita(TipoDeLancamento.Sentido sentido) {
        return this == RECEITA ? sentido == TipoDeLancamento.Sentido.ENTRADA : sentido == TipoDeLancamento.Sentido.SAIDA;
    }
}
