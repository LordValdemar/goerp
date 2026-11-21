package br.com.goerp.financeiro.dominio;

import java.util.List;
import java.util.Optional;

import br.com.goerp.compartilhado.dominio.Pagina;
import br.com.goerp.compartilhado.dominio.Paginacao;

/** Livro de lançamentos da empresa atual: só inclui, nunca altera. */
public interface LancamentoRepositorio {

    Lancamento registrar(Lancamento lancamento);

    Optional<Lancamento> buscarPorId(Long id);

    boolean foiEstornado(Long id);

    /** Mais recentes primeiro. */
    Pagina<Lancamento> extrato(Long contaId, FiltroDeExtrato filtro, Paginacao paginacao);

    /** Lançamentos (não estornos) gerados por um documento, ex.: as duas pontas de uma transferência. */
    List<Lancamento> doDocumento(OrigemDoLancamento origem, Long documentoDeOrigem);

    long proximoNumeroDeTransferencia(Long empresaId);
}
