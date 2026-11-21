package br.com.goerp.compras.dominio;

import java.util.List;
import java.util.Optional;

/** Recebimentos da empresa atual. */
public interface RecebimentoRepositorio {

    Recebimento salvar(Recebimento recebimento);

    Optional<Recebimento> buscarPorId(Long id);

    /** Do mais antigo para o mais recente. */
    List<Recebimento> doPedido(Long pedidoId);
}
