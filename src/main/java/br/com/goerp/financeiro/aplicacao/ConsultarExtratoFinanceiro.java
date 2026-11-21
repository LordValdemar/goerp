package br.com.goerp.financeiro.aplicacao;

import java.time.Instant;
import java.time.LocalDate;

import br.com.goerp.compartilhado.aplicacao.CasoDeUso;
import br.com.goerp.compartilhado.aplicacao.FusoHorario;
import br.com.goerp.compartilhado.dominio.Pagina;
import br.com.goerp.compartilhado.dominio.Paginacao;
import br.com.goerp.compartilhado.dominio.excecao.ValorInvalidoException;
import br.com.goerp.financeiro.dominio.FiltroDeExtrato;
import br.com.goerp.financeiro.dominio.FormaDePagamento;
import br.com.goerp.financeiro.dominio.Lancamento;
import br.com.goerp.financeiro.dominio.LancamentoRepositorio;

/** Extrato da conta: lançamentos com o saldo após cada um, filtrável por forma de pagamento e período. */
@CasoDeUso
public class ConsultarExtratoFinanceiro {

    private final BuscarContaFinanceira buscarConta;
    private final LancamentoRepositorio lancamentos;
    private final FusoHorario fuso;

    public ConsultarExtratoFinanceiro(BuscarContaFinanceira buscarConta, LancamentoRepositorio lancamentos,
            FusoHorario fuso) {
        this.buscarConta = buscarConta;
        this.lancamentos = lancamentos;
        this.fuso = fuso;
    }

    /** @param ate inclusive; datas nulas não limitam */
    public Pagina<Lancamento> executar(Long contaId, FormaDePagamento forma, LocalDate de, LocalDate ate,
            Paginacao paginacao) {
        buscarConta.executar(contaId);
        if (de != null && ate != null && ate.isBefore(de)) {
            throw new ValorInvalidoException("O fim do período não pode ser antes do início");
        }
        return lancamentos.extrato(contaId, new FiltroDeExtrato(forma, inicioDo(de), inicioDo(ate == null ? null
                : ate.plusDays(1))), paginacao);
    }

    private Instant inicioDo(LocalDate dia) {
        return dia == null ? null : dia.atStartOfDay(fuso.zona()).toInstant();
    }
}
