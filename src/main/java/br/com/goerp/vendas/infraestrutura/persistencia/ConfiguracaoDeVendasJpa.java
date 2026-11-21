package br.com.goerp.vendas.infraestrutura.persistencia;

import java.math.BigDecimal;

import org.hibernate.annotations.TenantId;
import org.hibernate.envers.Audited;

import br.com.goerp.compartilhado.infraestrutura.auditoria.EntidadeJpaAuditavel;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Audited
@Entity(name = "ConfiguracaoDeVendas")
@Table(name = "configuracao_venda")
class ConfiguracaoDeVendasJpa extends EntidadeJpaAuditavel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @TenantId
    @Column(name = "empresa_id", nullable = false, updatable = false)
    private Long empresaId;

    @Column(name = "desconto_maximo_percentual", nullable = false)
    private BigDecimal descontoMaximoPercentual;
}
