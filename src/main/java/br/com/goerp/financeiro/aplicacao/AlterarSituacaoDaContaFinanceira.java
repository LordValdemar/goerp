package br.com.goerp.financeiro.aplicacao;

import br.com.goerp.compartilhado.aplicacao.CasoDeUso;
import br.com.goerp.compartilhado.dominio.excecao.RecursoNaoEncontradoException;
import br.com.goerp.financeiro.dominio.ContaFinanceira;
import br.com.goerp.financeiro.dominio.ContaFinanceiraRepositorio;

/** Inativa (só com saldo zero) ou reativa. A conta é travada para nenhum lançamento entrar no meio. */
@CasoDeUso
public class AlterarSituacaoDaContaFinanceira {

    private final ContaFinanceiraRepositorio contas;

    public AlterarSituacaoDaContaFinanceira(ContaFinanceiraRepositorio contas) {
        this.contas = contas;
    }

    public ContaFinanceira executar(Long id, boolean ativar) {
        ContaFinanceira conta = contas.travar(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Conta financeira", id));
        if (ativar) {
            conta.reativar();
        } else {
            conta.inativar();
        }
        return contas.salvar(conta);
    }
}
