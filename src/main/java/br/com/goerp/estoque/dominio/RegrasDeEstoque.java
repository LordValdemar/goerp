package br.com.goerp.estoque.dominio;

/**
 * Regras que dependem do produto e da empresa.
 *
 * @param controlaEstoque  serviços e itens sem controle não recebem movimentações
 * @param aceitaFracao     vem da unidade do produto (KG aceita 1,250; UN só inteiros)
 * @param permiteNegativo  configuração da empresa: deixar o saldo ficar negativo em vez de recusar a saída
 */
public record RegrasDeEstoque(boolean controlaEstoque, boolean aceitaFracao, boolean permiteNegativo) {
}
