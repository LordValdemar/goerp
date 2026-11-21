package br.com.goerp.financeiro.aplicacao;

import static br.com.goerp.compartilhado.dominio.Textos.obrigatorio;

import java.util.Comparator;
import java.util.List;

import br.com.goerp.compartilhado.aplicacao.CasoDeUso;
import br.com.goerp.compartilhado.aplicacao.UsuarioAtual;
import br.com.goerp.compartilhado.dominio.excecao.RecursoNaoEncontradoException;
import br.com.goerp.financeiro.dominio.EstornoDeEstornoException;
import br.com.goerp.financeiro.dominio.Lancamento;
import br.com.goerp.financeiro.dominio.LancamentoDeOutraOrigemException;
import br.com.goerp.financeiro.dominio.LancamentoJaEstornadoException;
import br.com.goerp.financeiro.dominio.LancamentoRepositorio;
import br.com.goerp.financeiro.dominio.OrigemDoLancamento;

/**
 * Desfaz um lançamento avulso ou uma transferência (as duas pontas juntas). Lançamentos de
 * títulos e vendas são estornados pelo próprio título ou venda, para os dois lados não divergirem.
 */
@CasoDeUso
public class EstornarLancamento {

    private final LancamentoRepositorio lancamentos;
    private final LancadorFinanceiro lancador;
    private final UsuarioAtual usuarioAtual;

    public EstornarLancamento(LancamentoRepositorio lancamentos, LancadorFinanceiro lancador, UsuarioAtual usuarioAtual) {
        this.lancamentos = lancamentos;
        this.lancador = lancador;
        this.usuarioAtual = usuarioAtual;
    }

    public List<Lancamento> executar(Long lancamentoId, String motivo) {
        String justificativa = obrigatorio(motivo, "motivo");
        Lancamento lancamento = lancamentos.buscarPorId(lancamentoId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Lançamento", lancamentoId));
        if (lancamento.tipo().ehEstorno()) {
            throw new EstornoDeEstornoException();
        }
        List<Lancamento> aEstornar = switch (lancamento.origem()) {
            case MANUAL -> List.of(lancamento);
            case TRANSFERENCIA -> lancamentos.doDocumento(OrigemDoLancamento.TRANSFERENCIA,
                    lancamento.documentoDeOrigem());
            default -> throw new LancamentoDeOutraOrigemException(lancamento.origem());
        };
        if (aEstornar.stream().anyMatch(l -> lancamentos.foiEstornado(l.id()))) {
            throw new LancamentoJaEstornadoException();
        }
        return aEstornar.stream()
                .sorted(Comparator.comparing(Lancamento::contaId))
                .map(original -> lancador.lancar(original.contaId(),
                        original.pedidoDeEstorno(justificativa, usuarioAtual.id())))
                .toList();
    }
}
