package br.com.goerp.cadastros.produto.infraestrutura.persistencia;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import br.com.goerp.compartilhado.dominio.Gtin;

interface ProdutoJpaRepository extends JpaRepository<ProdutoJpa, Long>, JpaSpecificationExecutor<ProdutoJpa> {

    boolean existsByCodigo(String codigo);

    boolean existsByCodigoDeBarras(Gtin codigoDeBarras);

    boolean existsByCodigoDeBarrasAndIdNot(Gtin codigoDeBarras, Long id);
}
