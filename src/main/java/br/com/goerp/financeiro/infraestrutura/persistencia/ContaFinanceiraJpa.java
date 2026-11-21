package br.com.goerp.financeiro.infraestrutura.persistencia;

import java.math.BigDecimal;

import org.hibernate.annotations.OptimisticLock;
import org.hibernate.annotations.TenantId;
import org.hibernate.envers.Audited;
import org.hibernate.envers.NotAudited;

import br.com.goerp.compartilhado.infraestrutura.auditoria.EntidadeJpaAuditavel;
import br.com.goerp.financeiro.dominio.TipoDeConta;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Audited
@Entity(name = "ContaFinanceira")
@Table(name = "conta_financeira")
class ContaFinanceiraJpa extends EntidadeJpaAuditavel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @TenantId
    @Column(name = "empresa_id", nullable = false, updatable = false)
    private Long empresaId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, updatable = false)
    private TipoDeConta tipo;

    @Column(nullable = false)
    private String nome;

    @Column(name = "permite_saldo_negativo", nullable = false)
    private boolean permiteSaldoNegativo;

    /**
     * O histórico do saldo são os lançamentos (por isso fora do Envers), e um lançamento não pode
     * dar "conflito de edição" em quem está alterando o nome da conta (por isso fora da versão).
     */
    @NotAudited
    @OptimisticLock(excluded = true)
    @Column(nullable = false)
    private BigDecimal saldo;

    @Column(nullable = false)
    private boolean ativa;

    @NotAudited
    @Version
    @Column(nullable = false)
    private Long versao;
}
