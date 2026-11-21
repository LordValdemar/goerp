package br.com.goerp.cadastros.produto.infraestrutura.persistencia;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

interface UnidadeDeMedidaJpaRepository extends JpaRepository<UnidadeDeMedidaJpa, Long> {

    List<UnidadeDeMedidaJpa> findAllByOrderBySigla();

    List<UnidadeDeMedidaJpa> findByAtivaOrderBySigla(boolean ativa);

    boolean existsBySigla(String sigla);
}
