package br.com.goerp.cadastros.produto.dominio;

import java.util.List;
import java.util.Optional;

/** Categorias da empresa atual (o filtro por empresa é automático). */
public interface CategoriaRepositorio {

    Categoria salvar(Categoria categoria);

    Optional<Categoria> buscarPorId(Long id);

    /** @param ativa nulo traz todas */
    List<Categoria> listar(Boolean ativa);

    boolean existeComNome(String nome);

    boolean existeOutraComNome(String nome, Long idIgnorado);
}
