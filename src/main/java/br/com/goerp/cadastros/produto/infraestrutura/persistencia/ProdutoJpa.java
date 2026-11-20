package br.com.goerp.cadastros.produto.infraestrutura.persistencia;

import java.math.BigDecimal;

import org.hibernate.annotations.TenantId;
import org.hibernate.envers.Audited;
import org.hibernate.envers.NotAudited;

import br.com.goerp.cadastros.produto.dominio.OrigemDaMercadoria;
import br.com.goerp.compartilhado.dominio.Dinheiro;
import br.com.goerp.compartilhado.dominio.Gtin;
import br.com.goerp.compartilhado.dominio.Ncm;
import br.com.goerp.compartilhado.dominio.Quantidade;
import br.com.goerp.compartilhado.infraestrutura.auditoria.EntidadeJpaAuditavel;
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
@Entity(name = "Produto")
@Table(name = "produto")
class ProdutoJpa extends EntidadeJpaAuditavel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @TenantId
    @Column(name = "empresa_id", nullable = false, updatable = false)
    private Long empresaId;

    @Column(nullable = false, updatable = false)
    private String codigo;

    @Column(name = "codigo_barras")
    private Gtin codigoDeBarras;

    @Column(nullable = false)
    private String descricao;

    @Column(name = "categoria_id")
    private Long categoriaId;

    @Column(name = "unidade_id", nullable = false)
    private Long unidadeId;

    @Column(name = "preco_venda", nullable = false)
    private Dinheiro precoDeVenda;

    @Column(name = "preco_custo", nullable = false)
    private BigDecimal precoDeCusto;

    @Column(nullable = false)
    private Ncm ncm;

    private String cest;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrigemDaMercadoria origem;

    @Column(name = "controla_estoque", nullable = false)
    private boolean controlaEstoque;

    @Column(name = "estoque_minimo", nullable = false)
    private Quantidade estoqueMinimo;

    @Column(nullable = false)
    private boolean ativo;

    @NotAudited
    @Version
    @Column(nullable = false)
    private Long versao;
}
