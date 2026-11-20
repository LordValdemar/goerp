package br.com.goerp.estoque.infraestrutura.persistencia;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.goerp.estoque.dominio.SituacaoDoInventario;

interface InventarioJpaRepository extends JpaRepository<InventarioJpa, Long> {

    boolean existsBySituacao(SituacaoDoInventario situacao);

    List<InventarioJpa> findAllByOrderByIdDesc();
}
