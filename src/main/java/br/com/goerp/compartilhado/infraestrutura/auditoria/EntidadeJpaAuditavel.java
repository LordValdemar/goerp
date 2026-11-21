package br.com.goerp.compartilhado.infraestrutura.auditoria;

import java.time.OffsetDateTime;

import org.hibernate.envers.NotAudited;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;

/**
 * Base das entidades JPA: registra quem criou/alterou por último e quando.
 *
 * <p>O histórico completo fica no Envers (tabelas {@code *_aud} + {@link Revisao}). Estes campos
 * não entram no Envers porque a revisão já guarda quem e quando; assim, uma alteração que só
 * mexe neles não gera revisão vazia.
 */
@Getter
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class EntidadeJpaAuditavel {

    @NotAudited
    @CreatedDate
    @Column(name = "criado_em", nullable = false, updatable = false)
    private OffsetDateTime criadoEm;

    @NotAudited
    @CreatedBy
    @Column(name = "criado_por", updatable = false)
    private Long criadoPor;

    @NotAudited
    @LastModifiedDate
    @Column(name = "atualizado_em", nullable = false)
    private OffsetDateTime atualizadoEm;

    @NotAudited
    @LastModifiedBy
    @Column(name = "atualizado_por")
    private Long atualizadoPor;
}
