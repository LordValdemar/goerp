package br.com.goerp.financeiro.infraestrutura.persistencia;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import br.com.goerp.financeiro.dominio.OrigemDoTitulo;

interface TituloJpaRepository extends JpaRepository<TituloJpa, Long>, JpaSpecificationExecutor<TituloJpa> {

    List<TituloJpa> findByOrigemAndDocumentoDeOrigemOrderByParcela(OrigemDoTitulo origem, Long documentoDeOrigem);
}
