package br.com.goerp.usuario.infraestrutura.persistencia;

import java.time.Instant;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

interface SessaoDeAcessoJpaRepository extends JpaRepository<SessaoDeAcessoJpa, Long> {

    Optional<SessaoDeAcessoJpa> findByHashDoToken(String hashDoToken);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("UPDATE SessaoDeAcesso s SET s.revogadaEm = :agora WHERE s.usuarioId = :usuarioId AND s.revogadaEm IS NULL")
    void revogarTodasDoUsuario(Long usuarioId, Instant agora);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("""
            UPDATE SessaoDeAcesso s SET s.revogadaEm = :agora
            WHERE s.usuarioId = :usuarioId AND s.empresaId = :empresaId AND s.revogadaEm IS NULL""")
    void revogarTodasDoUsuarioNaEmpresa(Long usuarioId, Long empresaId, Instant agora);
}
