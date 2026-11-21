package br.com.goerp.estoque.web;

import java.math.BigDecimal;

import br.com.goerp.estoque.aplicacao.PosicaoDeEstoque;

public record PosicaoResponse(
        Long produtoId,
        String codigo,
        String descricao,
        String unidade,
        BigDecimal saldo,
        BigDecimal estoqueMinimo,
        boolean precisaRepor,
        BigDecimal custoMedio,
        BigDecimal valorEmEstoque) {

    static PosicaoResponse de(PosicaoDeEstoque posicao) {
        return new PosicaoResponse(posicao.produtoId(), posicao.codigo(), posicao.descricao(), posicao.unidade(),
                posicao.saldo().valor(), posicao.estoqueMinimo().valor(), posicao.precisaRepor(),
                posicao.custoMedio().valor(), posicao.valorEmEstoque().valor());
    }
}
