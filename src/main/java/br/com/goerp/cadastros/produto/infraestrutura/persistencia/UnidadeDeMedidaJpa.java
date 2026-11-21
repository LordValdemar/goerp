package br.com.goerp.cadastros.produto.infraestrutura.persistencia;

import org.hibernate.annotations.TenantId;
import org.hibernate.envers.Audited;

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
@Entity(name = "UnidadeDeMedida")
@Table(name = "unidade_medida")
class UnidadeDeMedidaJpa extends EntidadeJpaAuditavel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @TenantId
    @Column(name = "empresa_id", nullable = false, updatable = false)
    private Long empresaId;

    @Column(nullable = false, updatable = false)
    private String sigla;

    @Column(nullable = false)
    private String descricao;

    @Column(nullable = false)
    private boolean fracionavel;

    @Column(nullable = false)
    private boolean ativa;
}
