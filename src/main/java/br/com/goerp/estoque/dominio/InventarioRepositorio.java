package br.com.goerp.estoque.dominio;

import java.util.List;
import java.util.Optional;

/** Inventários da empresa atual. */
public interface InventarioRepositorio {

    Inventario salvar(Inventario inventario);

    Optional<Inventario> buscarPorId(Long id);

    boolean existeAberto();

    /** Do mais recente para o mais antigo. */
    List<Inventario> listar();
}
