package br.com.goerp.financeiro.infraestrutura.persistencia;

import java.math.BigDecimal;
import java.time.Instant;

import org.hibernate.annotations.TenantId;
import org.hibernate.envers.Audited;

import br.com.goerp.compartilhado.infraestrutura.auditoria.EntidadeJpaAuditavel;
import br.com.goerp.financeiro.dominio.FormaDePagamento;
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

/** Pagamento de um título, com id próprio: é o documento de origem do lançamento que ele gerou. */
@Getter
@Setter
@Audited
@Entity(name = "BaixaDeTitulo")
@Table(name = "baixa_titulo")
class BaixaJpa extends EntidadeJpaAuditavel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @TenantId
    @Column(name = "empresa_id", nullable = false, updatable = false)
    private Long empresaId;

    @Column(name = "titulo_id", nullable = false, updatable = false)
    private Long tituloId;

    @Column(nullable = false, updatable = false)
    private int numero;

    @Column(name = "conta_id", nullable = false, updatable = false)
    private Long contaId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, updatable = false)
    private FormaDePagamento forma;

    @Column(name = "valor_pago", nullable = false, updatable = false)
    private BigDecimal valorPago;

    @Column(nullable = false, updatable = false)
    private BigDecimal juros;

    @Column(nullable = false, updatable = false)
    private BigDecimal multa;

    @Column(nullable = false, updatable = false)
    private BigDecimal desconto;

    @Column(name = "usuario_id", updatable = false)
    private Long usuarioId;

    @Column(nullable = false, updatable = false)
    private Instant momento;

    @Column(nullable = false)
    private boolean estornada;

    @Column(name = "motivo_estorno")
    private String motivoDoEstorno;

    @Column(name = "estornada_em")
    private Instant estornadaEm;
}
