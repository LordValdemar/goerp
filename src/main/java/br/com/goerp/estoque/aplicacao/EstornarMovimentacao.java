package br.com.goerp.estoque.aplicacao;

import br.com.goerp.compartilhado.aplicacao.CasoDeUso;
import br.com.goerp.compartilhado.aplicacao.UsuarioAtual;
import br.com.goerp.compartilhado.dominio.excecao.RecursoNaoEncontradoException;
import br.com.goerp.estoque.dominio.Movimentacao;
import br.com.goerp.estoque.dominio.MovimentacaoJaEstornadaException;
import br.com.goerp.estoque.dominio.MovimentacaoRepositorio;

/** Desfaz uma movimentação lançando a inversa (a original continua no histórico). Só uma vez. */
@CasoDeUso
public class EstornarMovimentacao {

    private final MovimentacaoRepositorio movimentacoes;
    private final MovimentadorDeEstoque movimentador;
    private final UsuarioAtual usuarioAtual;

    public EstornarMovimentacao(MovimentacaoRepositorio movimentacoes, MovimentadorDeEstoque movimentador,
            UsuarioAtual usuarioAtual) {
        this.movimentacoes = movimentacoes;
        this.movimentador = movimentador;
        this.usuarioAtual = usuarioAtual;
    }

    public Movimentacao executar(Long movimentacaoId, String motivo) {
        Movimentacao original = movimentacoes.buscarPorId(movimentacaoId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Movimentação", movimentacaoId));
        if (movimentacoes.foiEstornada(movimentacaoId)) {
            throw new MovimentacaoJaEstornadaException();
        }
        return movimentador.movimentar(original.produtoId(), original.pedidoDeEstorno(motivo, usuarioAtual.id()));
    }
}
