package br.com.goerp.usuario.infraestrutura.persistencia;

import java.time.Instant;

import org.hibernate.envers.Audited;
import org.hibernate.envers.NotAudited;

import br.com.goerp.compartilhado.dominio.Email;
import br.com.goerp.compartilhado.infraestrutura.auditoria.EntidadeJpaAuditavel;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Audited
@Entity(name = "Usuario")
@Table(name = "usuario")
class UsuarioJpa extends EntidadeJpaAuditavel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nome;

    @Column(nullable = false, unique = true, updatable = false)
    private Email email;

    /** Fora do histórico: não faz sentido guardar hashes de senhas antigas. */
    @NotAudited
    @Column(name = "senha_hash", nullable = false)
    private String senhaHash;

    @Column(name = "administrador_plataforma", nullable = false, updatable = false)
    private boolean administradorDaPlataforma;

    @Column(nullable = false)
    private boolean ativo;

    /** Controle de login: muda a cada tentativa e já é registrado nos eventos de segurança. */
    @NotAudited
    @Column(name = "tentativas_falhas", nullable = false)
    private int tentativasFalhas;

    @NotAudited
    @Column(name = "bloqueado_ate")
    private Instant bloqueadoAte;
}
