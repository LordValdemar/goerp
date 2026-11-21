package br.com.goerp.vendas.infraestrutura.persistencia;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

interface ConfiguracaoDeVendasJpaRepository extends JpaRepository<ConfiguracaoDeVendasJpa, Long> {

    Optional<ConfiguracaoDeVendasJpa> findFirstBy();
}
