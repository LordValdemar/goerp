package br.com.goerp.cadastros.fornecedor.infraestrutura.persistencia;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import br.com.goerp.compartilhado.dominio.Documento;

interface FornecedorJpaRepository extends JpaRepository<FornecedorJpa, Long>, JpaSpecificationExecutor<FornecedorJpa> {

    boolean existsByDocumento(Documento documento);
}
