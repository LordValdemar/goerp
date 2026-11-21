package br.com.goerp.compras.aplicacao;

import java.time.Instant;
import java.util.List;

/** Porta de leitura de compras (filtrada por empresa automaticamente). */
public interface ConsultasDeCompras {

    /** Recebimentos em [de, ate), mais recentes primeiro; fornecedor opcional. */
    List<RecebimentosDoPeriodo.Linha> recebimentos(Instant de, Instant ate, Long fornecedorId, boolean incluirEstornados);
}
