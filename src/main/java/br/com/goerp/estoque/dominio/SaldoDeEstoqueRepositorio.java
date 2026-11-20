package br.com.goerp.estoque.dominio;

import java.util.Optional;

/** Saldos da empresa atual (o filtro por empresa é automático). */
public interface SaldoDeEstoqueRepositorio {

    /**
     * Devolve o saldo do produto TRAVADO até o fim da transação (cria com zero se ainda não existe).
     * Duas movimentações do mesmo produto ao mesmo tempo esperam uma pela outra: o saldo nunca é
     * calculado a partir de um valor desatualizado.
     */
    SaldoDeEstoque travarParaMovimentar(Long empresaId, Long produtoId);

    void salvar(SaldoDeEstoque saldo);

    Optional<SaldoDeEstoque> buscar(Long produtoId);
}
