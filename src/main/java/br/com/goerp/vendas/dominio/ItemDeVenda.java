package br.com.goerp.vendas.dominio;

import static br.com.goerp.compartilhado.dominio.Textos.obrigatorio;

import java.util.Objects;

import br.com.goerp.compartilhado.dominio.Dinheiro;
import br.com.goerp.compartilhado.dominio.Quantidade;
import br.com.goerp.compartilhado.dominio.excecao.ValorInvalidoException;

/**
 * Item vendido. Código, descrição e preço são copiados do cadastro no momento da venda:
 * se o produto mudar depois, a venda continua mostrando o que de fato foi vendido.
 */
public record ItemDeVenda(
        Long produtoId,
        String codigo,
        String descricao,
        Quantidade quantidade,
        Dinheiro precoUnitario,
        Dinheiro desconto) {

    public ItemDeVenda {
        Objects.requireNonNull(produtoId, "produtoId");
        codigo = obrigatorio(codigo, "codigo");
        descricao = obrigatorio(descricao, "descricao");
        Objects.requireNonNull(quantidade, "quantidade");
        precoUnitario = Objects.requireNonNull(precoUnitario, "precoUnitario").exigirNaoNegativo("precoUnitario");
        desconto = Objects.requireNonNullElse(desconto, Dinheiro.ZERO).exigirNaoNegativo("desconto");
        if (quantidade.compareTo(Quantidade.ZERO) <= 0) {
            throw new ValorInvalidoException("A quantidade do item precisa ser maior que zero");
        }
        if (desconto.compareTo(precoUnitario.multiplicar(quantidade)) > 0) {
            throw new ValorInvalidoException("O desconto do item não pode passar do valor do item");
        }
    }

    /** Preço x quantidade, antes do desconto. */
    public Dinheiro bruto() {
        return precoUnitario.multiplicar(quantidade);
    }

    public Dinheiro total() {
        return bruto().subtrair(desconto);
    }
}
