package br.com.goerp.estoque.aplicacao;

import br.com.goerp.compartilhado.aplicacao.CasoDeUso;
import br.com.goerp.compartilhado.dominio.excecao.RecursoNaoEncontradoException;
import br.com.goerp.estoque.dominio.Movimentacao;
import br.com.goerp.estoque.dominio.MovimentacaoRepositorio;

@CasoDeUso
public class BuscarMovimentacao {

    private final MovimentacaoRepositorio movimentacoes;

    public BuscarMovimentacao(MovimentacaoRepositorio movimentacoes) {
        this.movimentacoes = movimentacoes;
    }

    public Movimentacao executar(Long id) {
        return movimentacoes.buscarPorId(id).orElseThrow(() -> new RecursoNaoEncontradoException("Movimentação", id));
    }
}
