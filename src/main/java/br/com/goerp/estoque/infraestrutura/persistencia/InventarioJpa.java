package br.com.goerp.estoque.infraestrutura.persistencia;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.annotations.TenantId;
import org.hibernate.envers.Audited;
import org.hibernate.envers.NotAudited;

import br.com.goerp.compartilhado.infraestrutura.auditoria.EntidadeJpaAuditavel;
import br.com.goerp.estoque.dominio.SituacaoDoInventario;
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
@Entity(name = "Inventario")
@Table(name = "inventario")
class InventarioJpa extends EntidadeJpaAuditavel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @TenantId
    @Column(name = "empresa_id", nullable = false, updatable = false)
    private Long empresaId;

    @Column(nullable = false, updatable = false)
    private String descricao;

    @Column(name = "aberto_em", nullable = false, updatable = false)
    private Instant abertoEm;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SituacaoDoInventario situacao;

    @Column(name = "encerrado_em")
    private Instant encerradoEm;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "inventario_contagem", joinColumns = @JoinColumn(name = "inventario_id"))
    @OrderColumn(name = "posicao")
    private List<ContagemJpa> contagens = new ArrayList<>();

    /** Duas pessoas registrando contagens no mesmo instante: a segunda recebe 409 e repete. */
    @NotAudited
    @Version
    @Column(nullable = false)
    private Long versao;
}
