package br.com.goerp.compras.infraestrutura.persistencia;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

interface PedidoDeCompraJpaRepository extends JpaRepository<PedidoDeCompraJpa, Long>,
        JpaSpecificationExecutor<PedidoDeCompraJpa> {
}
