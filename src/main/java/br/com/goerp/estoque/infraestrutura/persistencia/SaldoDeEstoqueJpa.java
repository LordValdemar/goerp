package br.com.goerp.estoque.infraestrutura.persistencia;

import java.math.BigDecimal;

import org.hibernate.annotations.TenantId;

import br.com.goerp.compartilhado.dominio.Quantidade;
import br.com.goerp.compartilhado.infraestrutura.auditoria.EntidadeJpaAuditavel;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/** Não é auditado pelo Envers: o histórico do saldo são as próprias movimentações. */
@Getter
@Setter
@Entity(name = "SaldoDeEstoque")
@Table(name = "saldo_estoque")
class SaldoDeEstoqueJpa extends EntidadeJpaAuditavel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @TenantId
    @Column(name = "empresa_id", nullable = false, updatable = false)
    private Long empresaId;

    @Column(name = "produto_id", nullable = false, updatable = false)
    private Long produtoId;

    @Column(nullable = false)
    private Quantidade quantidade;

    @Column(name = "custo_medio", nullable = false)
    private BigDecimal custoMedio;
}
