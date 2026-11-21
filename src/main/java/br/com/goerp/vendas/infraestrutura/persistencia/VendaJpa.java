package br.com.goerp.vendas.infraestrutura.persistencia;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.annotations.TenantId;
import org.hibernate.envers.Audited;
import org.hibernate.envers.NotAudited;

import br.com.goerp.compartilhado.infraestrutura.auditoria.EntidadeJpaAuditavel;
import br.com.goerp.vendas.dominio.SituacaoDaVenda;
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

/**
 * Venda gravada. Itens e pagamentos não mudam depois de gravados (fora do Envers); o que muda
 * (cancelamento) é auditado.
 */
@Getter
@Setter
@Audited
@Entity(name = "Venda")
@Table(name = "venda")
class VendaJpa extends EntidadeJpaAuditavel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @TenantId
    @Column(name = "empresa_id", nullable = false, updatable = false)
    private Long empresaId;

    @Column(nullable = false, updatable = false)
    private Long numero;

    @Column(name = "caixa_id", nullable = false, updatable = false)
    private Long caixaId;

    @Column(name = "operador_id", nullable = false, updatable = false)
    private Long operadorId;

    @Column(name = "cliente_id", updatable = false)
    private Long clienteId;

    @Column(name = "cpf_na_nota", updatable = false)
    private String cpfNaNota;

    @Column(name = "desconto_na_venda", nullable = false, updatable = false)
    private BigDecimal descontoNaVenda;

    @Column(nullable = false, updatable = false)
    private BigDecimal total;

    @Column(nullable = false, updatable = false)
    private BigDecimal troco;

    @Column(nullable = false, updatable = false)
    private Instant momento;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SituacaoDaVenda situacao;

    @Column(name = "cancelada_em")
    private Instant canceladaEm;

    @Column(name = "motivo_cancelamento")
    private String motivoDoCancelamento;

    @Column(name = "cancelada_por")
    private Long canceladaPor;

    @NotAudited
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "venda_item", joinColumns = @JoinColumn(name = "venda_id"))
    @OrderColumn(name = "posicao")
    private List<ItemDeVendaJpa> itens = new ArrayList<>();

    @NotAudited
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "venda_pagamento", joinColumns = @JoinColumn(name = "venda_id"))
    @OrderColumn(name = "posicao")
    private List<PagamentoJpa> pagamentos = new ArrayList<>();

    @NotAudited
    @Version
    @Column(nullable = false)
    private Long versao;
}
