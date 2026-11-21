package br.com.goerp.financeiro.infraestrutura.persistencia;

import org.hibernate.annotations.TenantId;
import org.hibernate.envers.Audited;

import br.com.goerp.compartilhado.infraestrutura.auditoria.EntidadeJpaAuditavel;
import br.com.goerp.financeiro.dominio.TipoDeCategoria;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Audited
@Entity(name = "CategoriaFinanceira")
@Table(name = "categoria_financeira")
class CategoriaFinanceiraJpa extends EntidadeJpaAuditavel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @TenantId
    @Column(name = "empresa_id", nullable = false, updatable = false)
    private Long empresaId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, updatable = false)
    private TipoDeCategoria tipo;

    @Column(nullable = false)
    private String nome;

    @Column(nullable = false)
    private boolean ativa;
}
