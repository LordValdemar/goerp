package br.com.goerp.compras.infraestrutura.persistencia;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.annotations.TenantId;
import org.hibernate.envers.Audited;
import org.hibernate.envers.NotAudited;

import br.com.goerp.compartilhado.infraestrutura.auditoria.EntidadeJpaAuditavel;
import br.com.goerp.compras.dominio.SituacaoDoPedidoDeCompra;
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

@Getter
@Setter
@Audited
@Entity(name = "PedidoDeCompra")
@Table(name = "pedido_compra")
class PedidoDeCompraJpa extends EntidadeJpaAuditavel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @TenantId
    @Column(name = "empresa_id", nullable = false, updatable = false)
    private Long empresaId;

    @Column(nullable = false, updatable = false)
    private Long numero;

    @Column(name = "fornecedor_id", nullable = false, updatable = false)
    private Long fornecedorId;

    @Column(name = "comprador_id", nullable = false, updatable = false)
    private Long compradorId;

    @Column(name = "criado_no_pedido_em", nullable = false, updatable = false)
    private Instant criadoNoPedidoEm;

    @Column(name = "previsao_entrega")
    private LocalDate previsaoDeEntrega;

    private String observacoes;

    @Column(nullable = false)
    private BigDecimal total;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SituacaoDoPedidoDeCompra situacao;

    @Column(name = "enviado_em")
    private Instant enviadoEm;

    @Column(name = "encerrado_em")
    private Instant encerradoEm;

    @Column(name = "motivo_encerramento")
    private String motivoDoEncerramento;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "pedido_compra_item", joinColumns = @JoinColumn(name = "pedido_id"))
    @OrderColumn(name = "posicao")
    private List<ItemDeCompraJpa> itens = new ArrayList<>();

    @NotAudited
    @Version
    @Column(nullable = false)
    private Long versao;
}
