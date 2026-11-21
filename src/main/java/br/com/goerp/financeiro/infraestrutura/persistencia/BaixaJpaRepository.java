package br.com.goerp.financeiro.infraestrutura.persistencia;

import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

interface BaixaJpaRepository extends JpaRepository<BaixaJpa, Long> {

    List<BaixaJpa> findByTituloIdOrderByNumero(Long tituloId);

    List<BaixaJpa> findByTituloIdInOrderByNumero(Collection<Long> titulos);
}
