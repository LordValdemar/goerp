package br.com.goerp.empresa.infraestrutura.persistencia;

import org.hibernate.envers.Audited;

import br.com.goerp.compartilhado.dominio.Cnpj;
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
@Entity(name = "Empresa")
@Table(name = "empresa")
class EmpresaJpa extends EntidadeJpaAuditavel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, updatable = false)
    private Cnpj cnpj;

    @Column(name = "razao_social", nullable = false)
    private String razaoSocial;

    @Column(name = "nome_fantasia")
    private String nomeFantasia;

    @Column(nullable = false)
    private boolean ativa;
}
