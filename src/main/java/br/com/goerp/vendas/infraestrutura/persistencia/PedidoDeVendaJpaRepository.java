package br.com.goerp.vendas.infraestrutura.persistencia;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

interface PedidoDeVendaJpaRepository extends JpaRepository<PedidoDeVendaJpa, Long>,
        JpaSpecificationExecutor<PedidoDeVendaJpa> {
}
