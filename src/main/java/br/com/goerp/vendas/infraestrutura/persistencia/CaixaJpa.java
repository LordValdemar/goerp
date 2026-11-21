package br.com.goerp.vendas.infraestrutura.persistencia;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.annotations.TenantId;
import org.hibernate.envers.Audited;
import org.hibernate.envers.NotAudited;

import br.com.goerp.compartilhado.infraestrutura.auditoria.EntidadeJpaAuditavel;
import br.com.goerp.vendas.dominio.SituacaoDoCaixa;
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
@Entity(name = "Caixa")
@Table(name = "caixa")
class CaixaJpa extends EntidadeJpaAuditavel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @TenantId
    @Column(name = "empresa_id", nullable = false, updatable = false)
    private Long empresaId;

    @Column(name = "operador_id", nullable = false, updatable = false)
    private Long operadorId;

    @Column(name = "troco_inicial", nullable = false, updatable = false)
    private BigDecimal trocoInicial;

    @Column(name = "aberto_em", nullable = false, updatable = false)
    private Instant abertoEm;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SituacaoDoCaixa situacao;

    @Column(name = "fechado_em")
    private Instant fechadoEm;

    @Column(name = "fechado_por")
    private Long fechadoPor;

    /** Só recebe inclusões e cada item já traz quem e quando: dispensa o histórico do Envers. */
    @NotAudited
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "caixa_movimento", joinColumns = @JoinColumn(name = "caixa_id"))
    @OrderColumn(name = "posicao")
    private List<MovimentoDeCaixaJpa> movimentos = new ArrayList<>();

    /** Gravada uma única vez, no fechamento. */
    @NotAudited
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "caixa_conferencia", joinColumns = @JoinColumn(name = "caixa_id"))
    @OrderColumn(name = "posicao")
    private List<ConferenciaDeCaixaJpa> conferencias = new ArrayList<>();

    @NotAudited
    @Version
    @Column(nullable = false)
    private Long versao;
}
