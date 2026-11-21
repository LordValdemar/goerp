package br.com.goerp.cadastros.produto.dominio;

import static br.com.goerp.compartilhado.dominio.Textos.obrigatorio;

import java.util.Objects;

import br.com.goerp.compartilhado.dominio.Dinheiro;
import br.com.goerp.compartilhado.dominio.Gtin;
import br.com.goerp.compartilhado.dominio.Quantidade;
import br.com.goerp.compartilhado.dominio.excecao.ValorInvalidoException;

/**
 * Dados editáveis de um produto (tudo menos o código interno).
 *
 * @param codigoDeBarras opcional
 * @param categoriaId    opcional
 * @param estoqueMinimo  abaixo dele o produto aparece como "a repor"; zero se não controla estoque
 */
public record DadosDoProduto(
        String descricao,
        Gtin codigoDeBarras,
        Long categoriaId,
        Long unidadeId,
        Dinheiro precoDeVenda,
        CustoUnitario precoDeCusto,
        DadosFiscais fiscais,
        boolean controlaEstoque,
        Quantidade estoqueMinimo) {

    /** Limite da descrição na NF-e (campo xProd). */
    public static final int TAMANHO_MAXIMO_DA_DESCRICAO = 120;

    public DadosDoProduto {
        descricao = obrigatorio(descricao, "descricao");
        if (descricao.length() > TAMANHO_MAXIMO_DA_DESCRICAO) {
            throw new ValorInvalidoException("A descrição pode ter até " + TAMANHO_MAXIMO_DA_DESCRICAO + " caracteres");
        }
        Objects.requireNonNull(unidadeId, "unidadeId");
        Objects.requireNonNull(fiscais, "fiscais");
        precoDeVenda = Objects.requireNonNull(precoDeVenda, "precoDeVenda").exigirNaoNegativo("precoDeVenda");
        precoDeCusto = Objects.requireNonNullElse(precoDeCusto, CustoUnitario.ZERO);
        estoqueMinimo = controlaEstoque ? Objects.requireNonNullElse(estoqueMinimo, Quantidade.ZERO) : Quantidade.ZERO;
        if (estoqueMinimo.negativa()) {
            throw new ValorInvalidoException("O estoque mínimo não pode ser negativo");
        }
    }
}
