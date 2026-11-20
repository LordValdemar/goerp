package br.com.goerp.cadastros.cliente.dominio;

/** Situação do cliente perante o ICMS, exigida na NF-e (campo indIEDest). */
public enum IndicadorDeIe {
    /** Tem inscrição estadual: ela é obrigatória. */
    CONTRIBUINTE,
    /** Contribuinte isento de inscrição: não informa inscrição. */
    ISENTO,
    /** Consumidor final, pessoa física, etc.: não informa inscrição. */
    NAO_CONTRIBUINTE
}
