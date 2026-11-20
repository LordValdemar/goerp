package br.com.goerp.usuario.infraestrutura.persistencia;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.goerp.compartilhado.dominio.Email;

interface UsuarioJpaRepository extends JpaRepository<UsuarioJpa, Long> {

    Optional<UsuarioJpa> findByEmail(Email email);

    boolean existsByEmail(Email email);
}
