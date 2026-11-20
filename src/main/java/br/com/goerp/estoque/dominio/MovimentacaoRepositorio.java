package br.com.goerp.estoque.dominio;

import java.util.Optional;

/** Movimentações da empresa atual. Só inclui e consulta: movimentação não se altera nem se apaga. */
public interface MovimentacaoRepositorio {

    Movimentacao registrar(Movimentacao movimentacao);

    Optional<Movimentacao> buscarPorId(Long id);

    boolean foiEstornada(Long id);
}
