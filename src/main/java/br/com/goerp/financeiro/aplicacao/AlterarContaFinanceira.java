package br.com.goerp.financeiro.aplicacao;

import java.util.Objects;

import br.com.goerp.compartilhado.aplicacao.CasoDeUso;
import br.com.goerp.compartilhado.dominio.excecao.CampoObrigatorioException;
import br.com.goerp.compartilhado.dominio.excecao.ConflitoDeEdicaoException;
import br.com.goerp.financeiro.dominio.ContaFinanceira;
import br.com.goerp.financeiro.dominio.ContaFinanceiraJaCadastradaException;
import br.com.goerp.financeiro.dominio.ContaFinanceiraRepositorio;

@CasoDeUso
public class AlterarContaFinanceira {

    private final ContaFinanceiraRepositorio contas;
    private final BuscarContaFinanceira buscarConta;

    public AlterarContaFinanceira(ContaFinanceiraRepositorio contas, BuscarContaFinanceira buscarConta) {
        this.contas = contas;
        this.buscarConta = buscarConta;
    }

    public ContaFinanceira executar(Long id, Long versaoLida, String nome, boolean permiteSaldoNegativo) {
        if (versaoLida == null) {
            throw new CampoObrigatorioException("versao");
        }
        ContaFinanceira conta = buscarConta.executar(id);
        if (!Objects.equals(conta.versao(), versaoLida)) {
            throw new ConflitoDeEdicaoException("Conta financeira", id);
        }
        conta.alterar(nome, permiteSaldoNegativo);
        if (contas.existeOutraComNome(conta.nome(), id)) {
            throw new ContaFinanceiraJaCadastradaException(conta.nome());
        }
        return contas.salvar(conta);
    }
}
