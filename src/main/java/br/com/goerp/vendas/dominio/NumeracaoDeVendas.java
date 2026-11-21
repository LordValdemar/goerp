package br.com.goerp.vendas.dominio;

/**
 * Próximo número de venda da empresa: sequencial, sem repetição. Se a venda não for gravada
 * (transação desfeita), o número também volta, então não ficam buracos.
 */
public interface NumeracaoDeVendas {

    long proximo(Long empresaId);
}
