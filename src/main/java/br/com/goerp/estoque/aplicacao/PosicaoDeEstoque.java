package br.com.goerp.estoque.aplicacao;

import br.com.goerp.cadastros.produto.dominio.CustoUnitario;
import br.com.goerp.compartilhado.dominio.Dinheiro;
import br.com.goerp.compartilhado.dominio.Quantidade;

/** Uma linha do relatório de estoque: produto, saldo, custo e se precisa repor. */
public record PosicaoDeEstoque(
        Long produtoId,
        String codigo,
        String descricao,
        String unidade,
        Quantidade saldo,
        CustoUnitario custoMedio,
        Quantidade estoqueMinimo) {

    /** No mínimo ou abaixo dele (inclui sem estoque): hora de repor. */
    public boolean precisaRepor() {
        return saldo.compareTo(estoqueMinimo) <= 0;
    }

    /** Quanto o estoque vale a custo médio (saldo negativo vale zero). */
    public Dinheiro valorEmEstoque() {
        if (saldo.negativa()) {
            return Dinheiro.ZERO;
        }
        return Dinheiro.de(saldo.valor().multiply(custoMedio.valor()).setScale(Dinheiro.CASAS_DECIMAIS,
                Dinheiro.ARREDONDAMENTO));
    }
}
