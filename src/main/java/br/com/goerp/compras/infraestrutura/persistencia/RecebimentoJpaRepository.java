package br.com.goerp.compras.infraestrutura.persistencia;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

interface RecebimentoJpaRepository extends JpaRepository<RecebimentoJpa, Long> {

    List<RecebimentoJpa> findByPedidoIdOrderById(Long pedidoId);
}
