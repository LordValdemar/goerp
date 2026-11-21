package br.com.goerp.financeiro.infraestrutura.persistencia;

import java.math.BigDecimal;
import java.time.Instant;

import org.hibernate.annotations.Immutable;
import org.hibernate.annotations.TenantId;

import br.com.goerp.financeiro.dominio.FormaDePagamento;
import br.com.goerp.financeiro.dominio.OrigemDoLancamento;
import br.com.goerp.financeiro.dominio.TipoDeLancamento;
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
 * trilha de auditoria do dinheiro (quem, quando, quanto, como e por quê), por isso não usa o Envers.
 */
@Getter
@Setter
@Immutable
@Entity(name = "LancamentoFinanceiro")
@Table(name = "lancamento_financeiro")
class LancamentoJpa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @TenantId
    @Column(name = "empresa_id", nullable = false, updatable = false)
    private Long empresaId;

    @Column(name = "conta_id", nullable = false)
    private Long contaId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoDeLancamento tipo;

    @Column(nullable = false)
    private BigDecimal valor;

    @Column(name = "saldo_resultante", nullable = false)
    private BigDecimal saldoResultante;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FormaDePagamento forma;

    @Column(name = "categoria_id")
    private Long categoriaId;

    @Column(nullable = false)
    private String descricao;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrigemDoLancamento origem;

    @Column(name = "documento_origem")
    private Long documentoDeOrigem;

    @Column(name = "estornado_id")
    private Long estornado;

    @Column(name = "usuario_id")
    private Long usuarioId;

    @Column(nullable = false)
    private Instant momento;
}
