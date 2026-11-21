package br.com.goerp.estoque.dominio;

/** Configuração de estoque da empresa atual. */
public interface ConfiguracaoDeEstoqueRepositorio {

    /** Falso enquanto a empresa não configurar: o padrão é recusar saída sem saldo. */
    boolean permiteEstoqueNegativo();

    void definirPermiteEstoqueNegativo(Long empresaId, boolean permite);
}
