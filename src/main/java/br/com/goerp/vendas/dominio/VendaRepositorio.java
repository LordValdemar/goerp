package br.com.goerp.vendas.dominio;

import java.util.Optional;

/** Vendas da empresa atual. */
public interface VendaRepositorio {

    Venda salvar(Venda venda);

    Optional<Venda> buscarPorId(Long id);
}
