package br.com.goerp.usuario.infraestrutura.persistencia;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

interface PerfilJpaRepository extends JpaRepository<PerfilJpa, Long> {

    List<PerfilJpa> findAllByOrderByNome();

    boolean existsByNomeIgnoreCase(String nome);

    boolean existsByNomeIgnoreCaseAndIdNot(String nome, Long id);
}
