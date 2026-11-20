package br.com.goerp.estoque.infraestrutura.persistencia;

import java.math.BigDecimal;
import java.time.Instant;

import org.hibernate.annotations.Immutable;
import org.hibernate.annotations.TenantId;

import br.com.goerp.estoque.dominio.OrigemDaMovimentacao;
import br.com.goerp.estoque.dominio.TipoDeMovimentacao;
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

/**
 * Registro imutável ({@link Immutable}: o Hibernate ignora qualquer alteração). Ele próprio é a
 * trilha de auditoria do estoque (quem, quando, quanto, por quê), por isso não usa o Envers.
 */
@Getter
@Setter
@Immutable
@Entity(name = "MovimentacaoDeEstoque")
@Table(name = "movimentacao_estoque")
class MovimentacaoJpa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @TenantId
    @Column(name = "empresa_id", nullable = false, updatable = false)
    private Long empresaId;

    @Column(name = "produto_id", nullable = false)
    private Long produtoId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoDeMovimentacao tipo;

    @Column(nullable = false)
    private BigDecimal quantidade;

    @Column(name = "custo_unitario", nullable = false)
    private BigDecimal custoUnitario;

    @Column(name = "saldo_resultante", nullable = false)
    private BigDecimal saldoResultante;

    @Column(name = "custo_medio_resultante", nullable = false)
    private BigDecimal custoMedioResultante;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrigemDaMovimentacao origem;

    @Column(name = "documento_origem")
    private Long documentoDeOrigem;

    private String motivo;

    @Column(name = "estornada_id")
    private Long estornada;

    @Column(name = "usuario_id")
    private Long usuarioId;

    @Column(nullable = false)
    private Instant momento;
}
