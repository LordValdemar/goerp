package br.com.goerp.financeiro.aplicacao;

import br.com.goerp.compartilhado.aplicacao.CasoDeUso;
import br.com.goerp.compartilhado.aplicacao.EmpresaAtual;
import br.com.goerp.financeiro.dominio.ContaFinanceira;
import br.com.goerp.financeiro.dominio.ContaFinanceiraJaCadastradaException;
import br.com.goerp.financeiro.dominio.ContaFinanceiraRepositorio;
import br.com.goerp.financeiro.dominio.TipoDeConta;

@CasoDeUso
public class CadastrarContaFinanceira {

    private final ContaFinanceiraRepositorio contas;
    private final EmpresaAtual empresaAtual;

    public CadastrarContaFinanceira(ContaFinanceiraRepositorio contas, EmpresaAtual empresaAtual) {
        this.contas = contas;
        this.empresaAtual = empresaAtual;
    }

    public ContaFinanceira executar(TipoDeConta tipo, String nome, boolean permiteSaldoNegativo) {
        ContaFinanceira conta = ContaFinanceira.nova(empresaAtual.id(), tipo, nome, permiteSaldoNegativo);
        if (contas.existeComNome(conta.nome())) {
            throw new ContaFinanceiraJaCadastradaException(conta.nome());
        }
        return contas.salvar(conta);
    }
}
