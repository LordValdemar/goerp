package br.com.goerp.estoque.aplicacao;

import java.util.Comparator;
import java.util.List;

import br.com.goerp.compartilhado.aplicacao.CasoDeUso;
import br.com.goerp.compartilhado.aplicacao.UsuarioAtual;
import br.com.goerp.estoque.dominio.Movimentacao;
import br.com.goerp.estoque.dominio.MovimentacaoRepositorio;
import br.com.goerp.estoque.dominio.OrigemDaMovimentacao;

/**
 * Desfaz no estoque tudo o que um documento de outro módulo gerou (ex.: cancelamento de venda
 * devolve a mercadoria). Estorna em ordem de produto, pelo mesmo motivo das baixas: evitar deadlock.
 * Movimentações já estornadas são ignoradas.
 */
@CasoDeUso
public class EstornarMovimentacoesDoDocumento {

    private final MovimentacaoRepositorio movimentacoes;
    private final MovimentadorDeEstoque movimentador;
    private final UsuarioAtual usuarioAtual;

    public EstornarMovimentacoesDoDocumento(MovimentacaoRepositorio movimentacoes, MovimentadorDeEstoque movimentador,
            UsuarioAtual usuarioAtual) {
        this.movimentacoes = movimentacoes;
        this.movimentador = movimentador;
        this.usuarioAtual = usuarioAtual;
    }

    public List<Movimentacao> executar(OrigemDaMovimentacao origem, Long documentoDeOrigem, String motivo) {
        return movimentacoes.doDocumento(origem, documentoDeOrigem).stream()
                .filter(movimentacao -> !movimentacoes.foiEstornada(movimentacao.id()))
                .sorted(Comparator.comparing(Movimentacao::produtoId))
                .map(movimentacao -> movimentador.movimentar(movimentacao.produtoId(),
                        movimentacao.pedidoDeEstorno(motivo, usuarioAtual.id())))
                .toList();
    }
}
