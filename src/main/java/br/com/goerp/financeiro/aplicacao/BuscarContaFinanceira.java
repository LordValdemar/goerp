package br.com.goerp.financeiro.aplicacao;

import br.com.goerp.compartilhado.aplicacao.CasoDeUso;
import br.com.goerp.compartilhado.dominio.excecao.RecursoNaoEncontradoException;
import br.com.goerp.financeiro.dominio.ContaFinanceira;
import br.com.goerp.financeiro.dominio.ContaFinanceiraRepositorio;

@CasoDeUso
public class BuscarContaFinanceira {

    private final ContaFinanceiraRepositorio contas;

    public BuscarContaFinanceira(ContaFinanceiraRepositorio contas) {
        this.contas = contas;
    }

    public ContaFinanceira executar(Long id) {
        return contas.buscarPorId(id).orElseThrow(() -> new RecursoNaoEncontradoException("Conta financeira", id));
    }
}
