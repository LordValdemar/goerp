package br.com.goerp.financeiro.aplicacao;

import java.util.Objects;

import br.com.goerp.compartilhado.aplicacao.CasoDeUso;
import br.com.goerp.compartilhado.aplicacao.EmpresaAtual;
import br.com.goerp.compartilhado.aplicacao.UsuarioAtual;
import br.com.goerp.compartilhado.dominio.Dinheiro;
import br.com.goerp.financeiro.dominio.ContaFinanceiraRepositorio;
import br.com.goerp.financeiro.dominio.FormaDePagamento;
import br.com.goerp.financeiro.dominio.Lancamento;
import br.com.goerp.financeiro.dominio.LancamentoRepositorio;
import br.com.goerp.financeiro.dominio.OrigemDoLancamento;
import br.com.goerp.financeiro.dominio.PedidoDeLancamento;
import br.com.goerp.financeiro.dominio.TipoDeLancamento;
import br.com.goerp.financeiro.dominio.TransferenciaParaAMesmaContaException;

/** Tira de uma conta e põe em outra (ex.: depósito do cofre no banco): as duas pontas ou nenhuma. */
@CasoDeUso
public class TransferirEntreContas {

    private final LancadorFinanceiro lancador;
    private final ContaFinanceiraRepositorio contas;
    private final LancamentoRepositorio lancamentos;
    private final EmpresaAtual empresaAtual;
    private final UsuarioAtual usuarioAtual;

    public TransferirEntreContas(LancadorFinanceiro lancador, ContaFinanceiraRepositorio contas,
            LancamentoRepositorio lancamentos, EmpresaAtual empresaAtual, UsuarioAtual usuarioAtual) {
        this.lancador = lancador;
        this.contas = contas;
        this.lancamentos = lancamentos;
        this.empresaAtual = empresaAtual;
        this.usuarioAtual = usuarioAtual;
    }

    /** @param forma padrão: transferência */
    public Transferencia executar(Long origemId, Long destinoId, Dinheiro valor, FormaDePagamento forma,
            String descricao) {
        if (Objects.equals(origemId, destinoId)) {
            throw new TransferenciaParaAMesmaContaException();
        }
        FormaDePagamento meio = Objects.requireNonNullElse(forma, FormaDePagamento.TRANSFERENCIA);
        travarEmOrdem(origemId, destinoId);
        long numero = lancamentos.proximoNumeroDeTransferencia(empresaAtual.id());
        Lancamento saida = lancador.lancar(origemId, pedido(TipoDeLancamento.SAIDA, valor, meio, descricao, numero));
        Lancamento entrada = lancador.lancar(destinoId, pedido(TipoDeLancamento.ENTRADA, valor, meio, descricao, numero));
        return new Transferencia(numero, saida, entrada);
    }

    /** Sempre na mesma ordem (menor id primeiro): duas transferências opostas não se travam mutuamente. */
    private void travarEmOrdem(Long origemId, Long destinoId) {
        Long primeiro = Math.min(origemId, destinoId);
        Long segundo = Math.max(origemId, destinoId);
        contas.travar(primeiro);
        contas.travar(segundo);
    }

    private PedidoDeLancamento pedido(TipoDeLancamento tipo, Dinheiro valor, FormaDePagamento forma, String descricao,
            long numero) {
        return new PedidoDeLancamento(tipo, valor, forma, null, descricao, OrigemDoLancamento.TRANSFERENCIA, numero,
                null, usuarioAtual.id());
    }
}
