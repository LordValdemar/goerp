package br.com.goerp.estoque.infraestrutura.persistencia;

import org.springframework.data.jpa.repository.JpaRepository;

interface MovimentacaoJpaRepository extends JpaRepository<MovimentacaoJpa, Long> {

    boolean existsByEstornada(Long estornada);
}
