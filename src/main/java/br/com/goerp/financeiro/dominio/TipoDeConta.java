package br.com.goerp.financeiro.dominio;

/** Onde o dinheiro fica guardado. */
public enum TipoDeConta {
    /** Dinheiro físico da empresa (cofre, tesouraria). */
    CAIXA,
    BANCO,
    /** Maquininha de cartão, carteira PIX e afins. */
    CONTA_DE_PAGAMENTO
}
