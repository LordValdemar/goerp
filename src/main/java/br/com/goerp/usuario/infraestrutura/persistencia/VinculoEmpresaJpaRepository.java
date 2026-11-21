package br.com.goerp.usuario.infraestrutura.persistencia;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

interface VinculoEmpresaJpaRepository extends JpaRepository<VinculoEmpresaJpa, Long> {

    Optional<VinculoEmpresaJpa> findByUsuarioIdAndEmpresaId(Long usuarioId, Long empresaId);

    List<VinculoEmpresaJpa> findByUsuarioIdOrderByEmpresaId(Long usuarioId);

    boolean existsByPerfilIdAndAtivoTrue(Long perfilId);

    long countByPerfilIdAndAtivoTrue(Long perfilId);

    List<VinculoEmpresaJpa> findAllByOrderById();
}
