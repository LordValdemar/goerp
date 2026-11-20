package br.com.goerp.cadastros.produto.infraestrutura.persistencia;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

interface CategoriaJpaRepository extends JpaRepository<CategoriaJpa, Long> {

    List<CategoriaJpa> findAllByOrderByNome();

    List<CategoriaJpa> findByAtivaOrderByNome(boolean ativa);

    boolean existsByNomeIgnoreCase(String nome);

    boolean existsByNomeIgnoreCaseAndIdNot(String nome, Long id);
}
