package br.com.goerp.financeiro.infraestrutura.persistencia;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import br.com.goerp.financeiro.dominio.OrigemDoTitulo;
import jakarta.persistence.LockModeType;

interface TituloJpaRepository extends JpaRepository<TituloJpa, Long>, JpaSpecificationExecutor<TituloJpa> {

    /** SELECT ... FOR UPDATE: segura a linha até o fim da transação. */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT t FROM Titulo t WHERE t.id = :id")
    Optional<TituloJpa> travar(Long id);

    List<TituloJpa> findByOrigemAndDocumentoDeOrigemOrderByParcela(OrigemDoTitulo origem, Long documentoDeOrigem);
}
