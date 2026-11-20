package br.com.goerp.usuario.dominio;

import java.util.Optional;

import br.com.goerp.compartilhado.dominio.Email;

public interface UsuarioRepositorio {

    Usuario salvar(Usuario usuario);

    Optional<Usuario> buscarPorId(Long id);

    Optional<Usuario> buscarPorEmail(Email email);

    boolean existeComEmail(Email email);
}
