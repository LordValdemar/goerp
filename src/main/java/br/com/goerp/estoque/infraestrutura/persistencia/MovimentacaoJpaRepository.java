package br.com.goerp.estoque.infraestrutura.persistencia;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

interface MovimentacaoJpaRepository extends JpaRepository<MovimentacaoJpa, Long> {

    boolean existsByEstornada(Long estornada);

    Page<MovimentacaoJpa> findByProdutoId(Long produtoId, Pageable paginacao);

    List<MovimentacaoJpa> findByOrigemAndDocumentoDeOrigemAndEstornadaIsNull(
            br.com.goerp.estoque.dominio.OrigemDaMovimentacao origem, Long documentoDeOrigem);
}
