package br.com.goerp.usuario.dominio;

import java.util.List;
import java.util.Optional;

/** Perfis da empresa atual (o filtro por empresa é automático). */
public interface PerfilRepositorio {

    Perfil salvar(Perfil perfil);

    Optional<Perfil> buscarPorId(Long id);

    List<Perfil> listar();

    boolean existeComNome(String nome);

    boolean existeOutroComNome(String nome, Long idIgnorado);
}
