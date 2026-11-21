package br.com.goerp.financeiro.infraestrutura.persistencia;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import br.com.goerp.financeiro.dominio.OrigemDoLancamento;

interface LancamentoJpaRepository extends JpaRepository<LancamentoJpa, Long>, JpaSpecificationExecutor<LancamentoJpa> {

    boolean existsByEstornado(Long estornado);

    List<LancamentoJpa> findByOrigemAndDocumentoDeOrigemAndEstornadoIsNullOrderById(OrigemDoLancamento origem,
            Long documentoDeOrigem);
}
