package br.com.goerp.compras.infraestrutura.persistencia;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.annotations.TenantId;
import org.hibernate.envers.Audited;
import org.hibernate.envers.NotAudited;

import br.com.goerp.compartilhado.infraestrutura.auditoria.EntidadeJpaAuditavel;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/** Itens não mudam depois de gravados (fora do Envers); o estorno é auditado. */
@Getter
@Setter
@Audited
@Entity(name = "Recebimento")
@Table(name = "recebimento")
class RecebimentoJpa extends EntidadeJpaAuditavel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @TenantId
    @Column(name = "empresa_id", nullable = false, updatable = false)
    private Long empresaId;

    @Column(name = "pedido_id", nullable = false, updatable = false)
    private Long pedidoId;

    @Column(name = "nota_fiscal", nullable = false, updatable = false)
    private String notaFiscal;

    @Column(name = "valor_total", nullable = false, updatable = false)
    private BigDecimal valorTotal;

    @Column(name = "recebido_por", nullable = false, updatable = false)
    private Long recebidoPor;

    @Column(name = "recebido_em", nullable = false, updatable = false)
    private Instant recebidoEm;

    @Column(nullable = false)
    private boolean estornado;

    @Column(name = "estornado_em")
    private Instant estornadoEm;

    @Column(name = "motivo_estorno")
    private String motivoDoEstorno;

    @NotAudited
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "recebimento_item", joinColumns = @JoinColumn(name = "recebimento_id"))
    @OrderColumn(name = "posicao")
    private List<ItemRecebidoJpa> itens = new ArrayList<>();
}
