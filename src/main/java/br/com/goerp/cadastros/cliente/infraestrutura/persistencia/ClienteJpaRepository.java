package br.com.goerp.cadastros.cliente.infraestrutura.persistencia;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import br.com.goerp.compartilhado.dominio.Documento;

interface ClienteJpaRepository extends JpaRepository<ClienteJpa, Long>, JpaSpecificationExecutor<ClienteJpa> {

    boolean existsByDocumento(Documento documento);
}
