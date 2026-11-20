package br.com.goerp.usuario.infraestrutura.persistencia;

import java.time.Instant;

import br.com.goerp.compartilhado.infraestrutura.auditoria.EntidadeJpaAuditavel;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/** Não é auditada pelo Envers: os eventos de segurança já registram abertura, renovação e encerramento. */
@Getter
@Setter
@Entity(name = "SessaoDeAcesso")
@Table(name = "sessao_acesso")
class SessaoDeAcessoJpa extends EntidadeJpaAuditavel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "usuario_id", nullable = false, updatable = false)
    private Long usuarioId;

    @Column(name = "empresa_id", updatable = false)
    private Long empresaId;

    @Column(name = "hash_token", nullable = false, unique = true, updatable = false)
    private String hashDoToken;

    @Column(name = "expira_em", nullable = false, updatable = false)
    private Instant expiraEm;

    @Column(name = "revogada_em")
    private Instant revogadaEm;
}
