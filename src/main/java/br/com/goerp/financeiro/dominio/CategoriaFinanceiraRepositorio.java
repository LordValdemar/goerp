package br.com.goerp.financeiro.dominio;

import java.util.List;
import java.util.Optional;

/** Categorias financeiras da empresa atual (o filtro por empresa é automático). */
public interface CategoriaFinanceiraRepositorio {

    CategoriaFinanceira salvar(CategoriaFinanceira categoria);

    Optional<CategoriaFinanceira> buscarPorId(Long id);

    /** Filtros nulos não restringem. */
    List<CategoriaFinanceira> listar(TipoDeCategoria tipo, Boolean ativa);

    boolean existeComNome(String nome);

    boolean existeOutraComNome(String nome, Long idIgnorado);
}
