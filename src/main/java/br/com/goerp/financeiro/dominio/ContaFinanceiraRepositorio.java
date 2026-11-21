package br.com.goerp.financeiro.dominio;

import java.util.List;
import java.util.Optional;

/** Contas da empresa atual (o filtro por empresa é automático). */
public interface ContaFinanceiraRepositorio {

    /** Grava os dados da conta; o saldo só é gravado por {@link #atualizarSaldo}. */
    ContaFinanceira salvar(ContaFinanceira conta);

    Optional<ContaFinanceira> buscarPorId(Long id);

    /** Busca e trava a conta até o fim da transação (lançamentos simultâneos esperam a vez). */
    Optional<ContaFinanceira> travar(Long id);

    void atualizarSaldo(ContaFinanceira conta);

    /** @param ativa nulo traz todas */
    List<ContaFinanceira> listar(Boolean ativa);

    boolean existeComNome(String nome);

    boolean existeOutraComNome(String nome, Long idIgnorado);
}
