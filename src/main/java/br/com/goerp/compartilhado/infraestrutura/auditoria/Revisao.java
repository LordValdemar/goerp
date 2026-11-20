package br.com.goerp.compartilhado.infraestrutura.auditoria;

import java.time.Instant;

import org.hibernate.envers.RevisionEntity;
import org.hibernate.envers.RevisionNumber;
import org.hibernate.envers.RevisionTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

/**
 * Revisão do Envers: agrupa as alterações de uma transação e registra
 * quem as fez, em qual empresa, quando e de qual IP.
 */
@Getter
@Setter(AccessLevel.PACKAGE)
@Entity
@Table(name = "revisao")
@RevisionEntity(RevisaoListener.class)
public class Revisao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @RevisionNumber
    private Long id;

    @RevisionTimestamp
    @Column(nullable = false)
    private long momento;

    @Column(name = "usuario_id")
    private Long usuarioId;

    @Column(name = "empresa_id")
    private Long empresaId;

    private String ip;

    public Instant momentoComoInstant() {
        return Instant.ofEpochMilli(momento);
    }
}
