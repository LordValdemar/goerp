package br.com.goerp.financeiro.dominio;

/** Quem gerou o lançamento; com o documento de origem, leva até a venda, título ou transferência. */
public enum OrigemDoLancamento {
    MANUAL,
    TRANSFERENCIA,
    BAIXA_DE_TITULO,
    VENDA,
    FECHAMENTO_DE_CAIXA
}
