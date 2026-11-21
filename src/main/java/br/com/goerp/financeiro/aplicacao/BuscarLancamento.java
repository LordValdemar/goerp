package br.com.goerp.financeiro.aplicacao;

import br.com.goerp.compartilhado.aplicacao.CasoDeUso;
import br.com.goerp.compartilhado.dominio.excecao.RecursoNaoEncontradoException;
import br.com.goerp.financeiro.dominio.Lancamento;
import br.com.goerp.financeiro.dominio.LancamentoRepositorio;

@CasoDeUso
public class BuscarLancamento {

    private final LancamentoRepositorio lancamentos;

    public BuscarLancamento(LancamentoRepositorio lancamentos) {
        this.lancamentos = lancamentos;
    }

    public Lancamento executar(Long id) {
        return lancamentos.buscarPorId(id).orElseThrow(() -> new RecursoNaoEncontradoException("Lançamento", id));
    }
}
