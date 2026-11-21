package br.com.goerp.financeiro.infraestrutura.persistencia;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

import org.hibernate.annotations.TenantId;
import org.hibernate.envers.Audited;
import org.hibernate.envers.NotAudited;

import br.com.goerp.compartilhado.infraestrutura.auditoria.EntidadeJpaAuditavel;
import br.com.goerp.financeiro.dominio.FormaDePagamento;
import br.com.goerp.financeiro.dominio.NaturezaDoTitulo;
import br.com.goerp.financeiro.dominio.OrigemDoTitulo;
import br.com.goerp.financeiro.dominio.SituacaoDoTitulo;
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
@Entity(name = "Titulo")
@Table(name = "titulo")
class TituloJpa extends EntidadeJpaAuditavel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @TenantId
    @Column(name = "empresa_id", nullable = false, updatable = false)
    private Long empresaId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, updatable = false)
    private NaturezaDoTitulo natureza;

    /** Fornecedor (a pagar) e cliente (a receber) em colunas separadas, cada uma com sua chave estrangeira. */
    @Column(name = "fornecedor_id", updatable = false)
    private Long fornecedorId;

    @Column(name = "cliente_id", updatable = false)
    private Long clienteId;

    @Column(nullable = false)
    private String descricao;

    @Column(name = "categoria_id")
    private Long categoriaId;

    private String documento;

    @Enumerated(EnumType.STRING)
    @Column(name = "forma_prevista", nullable = false, updatable = false)
    private FormaDePagamento formaPrevista;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, updatable = false)
    private OrigemDoTitulo origem;

    @Column(name = "documento_origem", updatable = false)
    private Long documentoDeOrigem;

    @Column(nullable = false, updatable = false)
    private int parcela;

    @Column(nullable = false, updatable = false)
    private int parcelas;

    @Column(nullable = false)
    private BigDecimal valor;

    @Column(name = "saldo_devedor", nullable = false)
    private BigDecimal saldoDevedor;

    @Column(nullable = false)
    private LocalDate vencimento;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SituacaoDoTitulo situacao;

    @Column(name = "motivo_cancelamento")
    private String motivoDoCancelamento;

    @Column(name = "cancelado_em")
    private Instant canceladoEm;

    @NotAudited
    @Version
    @Column(nullable = false)
    private Long versao;
}
