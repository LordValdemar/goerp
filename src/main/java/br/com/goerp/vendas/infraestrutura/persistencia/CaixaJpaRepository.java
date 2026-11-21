package br.com.goerp.vendas.infraestrutura.persistencia;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.goerp.vendas.dominio.SituacaoDoCaixa;

interface CaixaJpaRepository extends JpaRepository<CaixaJpa, Long> {

    Optional<CaixaJpa> findByOperadorIdAndSituacao(Long operadorId, SituacaoDoCaixa situacao);

    List<CaixaJpa> findAllByOrderByIdDesc();

    List<CaixaJpa> findBySituacaoOrderByIdDesc(SituacaoDoCaixa situacao);
}
