package br.com.goerp.estoque.infraestrutura.persistencia;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

interface ConfiguracaoDeEstoqueJpaRepository extends JpaRepository<ConfiguracaoDeEstoqueJpa, Long> {

    /** Cada empresa tem no máximo uma configuração; o filtro de empresa é automático. */
    Optional<ConfiguracaoDeEstoqueJpa> findFirstBy();
}
