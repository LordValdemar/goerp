package br.com.goerp.usuario.infraestrutura.persistencia;

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
@Entity(name = "VinculoEmpresa")
@Table(name = "usuario_empresa")
class VinculoEmpresaJpa extends EntidadeJpaAuditavel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "usuario_id", nullable = false, updatable = false)
    private Long usuarioId;

    @Column(name = "empresa_id", nullable = false, updatable = false)
    private Long empresaId;

    @Column(nullable = false)
    private boolean ativo;
}
