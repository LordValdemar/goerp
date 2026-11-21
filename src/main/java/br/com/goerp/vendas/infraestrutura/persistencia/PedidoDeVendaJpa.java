package br.com.goerp.vendas.infraestrutura.persistencia;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.annotations.TenantId;
import org.hibernate.envers.Audited;
import org.hibernate.envers.NotAudited;

import br.com.goerp.compartilhado.infraestrutura.auditoria.EntidadeJpaAuditavel;
import br.com.goerp.vendas.dominio.SituacaoDoPedido;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.Getter;
import lombok.Setter;

/** Pedido e itens auditados: o histórico mostra cada versão do orçamento negociado com o cliente. */
@Getter
@Setter
@Audited
@Entity(name = "PedidoDeVenda")
@Table(name = "pedido_venda")
class PedidoDeVendaJpa extends EntidadeJpaAuditavel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @TenantId
    @Column(name = "empresa_id", nullable = false, updatable = false)
    private Long empresaId;

    @Column(nullable = false, updatable = false)
    private Long numero;

    @Column(name = "cliente_id", nullable = false, updatable = false)
    private Long clienteId;

    @Column(name = "vendedor_id", nullable = false, updatable = false)
    private Long vendedorId;

    @Column(name = "criado_no_pedido_em", nullable = false, updatable = false)
    private Instant criadoNoPedidoEm;

    @Column(nullable = false)
    private BigDecimal desconto;

    @Column(nullable = false)
    private BigDecimal total;

    @Column(nullable = false)
    private LocalDate validade;

    private String observacoes;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SituacaoDoPedido situacao;

    @Column(name = "confirmado_em")
    private Instant confirmadoEm;

    @Column(name = "faturado_em")
    private Instant faturadoEm;

    @Column(name = "cancelado_em")
    private Instant canceladoEm;

    @Column(name = "motivo_cancelamento")
    private String motivoDoCancelamento;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "pedido_venda_item", joinColumns = @JoinColumn(name = "pedido_id"))
    @OrderColumn(name = "posicao")
    private List<ItemDeVendaJpa> itens = new ArrayList<>();

    @NotAudited
    @Version
    @Column(nullable = false)
    private Long versao;
}
