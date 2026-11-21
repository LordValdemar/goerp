package br.com.goerp.financeiro.infraestrutura.persistencia;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;


interface ContaFinanceiraJpaRepository extends JpaRepository<ContaFinanceiraJpa, Long> {

    List<ContaFinanceiraJpa> findAllByOrderByNome();

    List<ContaFinanceiraJpa> findByAtivaOrderByNome(boolean ativa);

    boolean existsByNomeIgnoreCase(String nome);

    boolean existsByNomeIgnoreCaseAndIdNot(String nome, Long id);
}
