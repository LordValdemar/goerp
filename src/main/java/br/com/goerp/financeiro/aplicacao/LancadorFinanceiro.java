package br.com.goerp.financeiro.aplicacao;

import java.time.Clock;

import br.com.goerp.compartilhado.aplicacao.ServicoDeAplicacao;
import br.com.goerp.compartilhado.dominio.excecao.RecursoNaoEncontradoException;
import br.com.goerp.financeiro.dominio.CategoriaFinanceiraRepositorio;
import br.com.goerp.financeiro.dominio.ContaFinanceira;
import br.com.goerp.financeiro.dominio.ContaFinanceiraRepositorio;
import br.com.goerp.financeiro.dominio.Lancamento;
import br.com.goerp.financeiro.dominio.LancamentoRepositorio;
import br.com.goerp.financeiro.dominio.PedidoDeLancamento;

/**
 * Único caminho para mexer no saldo de uma conta: trava a conta, aplica as regras, grava o novo
 * saldo e o lançamento. Roda na transação de quem chama (lançamento avulso, baixa de título,
 * venda...): se a operação maior falhar, o lançamento também é desfeito.
 */
@ServicoDeAplicacao
public class LancadorFinanceiro {

    private final ContaFinanceiraRepositorio contas;
    private final CategoriaFinanceiraRepositorio categorias;
    private final LancamentoRepositorio lancamentos;
    private final Clock relogio;

    public LancadorFinanceiro(ContaFinanceiraRepositorio contas, CategoriaFinanceiraRepositorio categorias,
            LancamentoRepositorio lancamentos, Clock relogio) {
        this.contas = contas;
        this.categorias = categorias;
        this.lancamentos = lancamentos;
        this.relogio = relogio;
    }

    public Lancamento lancar(Long contaId, PedidoDeLancamento pedido) {
        // o estorno herda a categoria do original, que já foi validada naquele sentido
        if (pedido.categoriaId() != null && !pedido.tipo().ehEstorno()) {
            categorias.buscarPorId(pedido.categoriaId())
                    .orElseThrow(() -> new RecursoNaoEncontradoException("Categoria financeira", pedido.categoriaId()))
                    .exigirQueClassifica(pedido.tipo().sentido());
        }
        ContaFinanceira conta = contas.travar(contaId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Conta financeira", contaId));
        Lancamento lancamento = conta.lancar(pedido, relogio.instant());
        contas.atualizarSaldo(conta);
        return lancamentos.registrar(lancamento);
    }
}
