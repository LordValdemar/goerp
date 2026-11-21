package br.com.goerp.cadastros.produto.dominio;

import java.util.List;
import java.util.Optional;

/** Unidades da empresa atual (o filtro por empresa é automático). */
public interface UnidadeDeMedidaRepositorio {

    UnidadeDeMedida salvar(UnidadeDeMedida unidade);

    Optional<UnidadeDeMedida> buscarPorId(Long id);

    /** @param ativa nulo traz todas */
    List<UnidadeDeMedida> listar(Boolean ativa);

    boolean existeComSigla(String sigla);
}
