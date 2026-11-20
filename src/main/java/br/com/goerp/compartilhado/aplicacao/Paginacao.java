package br.com.goerp.compartilhado.aplicacao;

/** Qual página buscar. A primeira página é a 0. O tamanho é limitado para proteger o banco. */
public record Paginacao(int numero, int tamanho) {

    public static final int TAMANHO_PADRAO = 20;
    public static final int TAMANHO_MAXIMO = 100;

    public Paginacao {
        numero = Math.max(numero, 0);
        tamanho = tamanho <= 0 ? TAMANHO_PADRAO : Math.min(tamanho, TAMANHO_MAXIMO);
    }

    public static Paginacao primeira() {
        return new Paginacao(0, TAMANHO_PADRAO);
    }
}
