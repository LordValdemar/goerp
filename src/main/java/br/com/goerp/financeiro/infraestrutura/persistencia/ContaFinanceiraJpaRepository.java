package br.com.goerp.financeiro.infraestrutura.persistencia;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import jakarta.persistence.LockModeType;

interface ContaFinanceiraJpaRepository extends JpaRepository<ContaFinanceiraJpa, Long> {

    /** SELECT ... FOR UPDATE: segura a linha até o fim da transação. */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT c FROM ContaFinanceira c WHERE c.id = :id")
    Optional<ContaFinanceiraJpa> travar(Long id);

    List<ContaFinanceiraJpa> findAllByOrderByNome();

    List<ContaFinanceiraJpa> findByAtivaOrderByNome(boolean ativa);

    boolean existsByNomeIgnoreCase(String nome);

    boolean existsByNomeIgnoreCaseAndIdNot(String nome, Long id);
}
