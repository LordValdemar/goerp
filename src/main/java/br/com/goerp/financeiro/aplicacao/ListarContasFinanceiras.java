package br.com.goerp.financeiro.aplicacao;

import java.util.List;

import br.com.goerp.compartilhado.aplicacao.CasoDeUso;
import br.com.goerp.financeiro.dominio.ContaFinanceira;
import br.com.goerp.financeiro.dominio.ContaFinanceiraRepositorio;

@CasoDeUso
public class ListarContasFinanceiras {

    private final ContaFinanceiraRepositorio contas;

    public ListarContasFinanceiras(ContaFinanceiraRepositorio contas) {
        this.contas = contas;
    }

    /** @param ativa nulo traz todas */
    public List<ContaFinanceira> executar(Boolean ativa) {
        return contas.listar(ativa);
    }
}
