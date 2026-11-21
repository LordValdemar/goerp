package br.com.goerp.financeiro.infraestrutura.persistencia;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

interface CategoriaFinanceiraJpaRepository extends JpaRepository<CategoriaFinanceiraJpa, Long>,
        JpaSpecificationExecutor<CategoriaFinanceiraJpa> {

    boolean existsByNomeIgnoreCase(String nome);

    boolean existsByNomeIgnoreCaseAndIdNot(String nome, Long id);
}
