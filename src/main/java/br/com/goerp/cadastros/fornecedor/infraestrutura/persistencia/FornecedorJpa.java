package br.com.goerp.cadastros.fornecedor.infraestrutura.persistencia;

import org.hibernate.annotations.TenantId;
import org.hibernate.envers.Audited;
import org.hibernate.envers.NotAudited;

import br.com.goerp.compartilhado.dominio.Documento;
import br.com.goerp.compartilhado.dominio.Email;
import br.com.goerp.compartilhado.dominio.Telefone;
import br.com.goerp.compartilhado.infraestrutura.auditoria.EntidadeJpaAuditavel;
import br.com.goerp.compartilhado.infraestrutura.persistencia.Conversores;
import br.com.goerp.compartilhado.infraestrutura.persistencia.EnderecoJpa;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
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
@Entity(name = "Fornecedor")
@Table(name = "fornecedor")
class FornecedorJpa extends EntidadeJpaAuditavel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @TenantId
    @Column(name = "empresa_id", nullable = false, updatable = false)
    private Long empresaId;

    @Convert(converter = Conversores.DocumentoConverter.class)
    @Column(nullable = false, updatable = false)
    private Documento documento;

    @Column(nullable = false)
    private String nome;

    @Column(name = "nome_fantasia")
    private String nomeFantasia;

    @Column(name = "inscricao_estadual")
    private String inscricaoEstadual;

    private Email email;

    private Telefone telefone;

    @Column(name = "pessoa_contato")
    private String pessoaDeContato;

    @Embedded
    private EnderecoJpa endereco;

    private String observacoes;

    @Column(nullable = false)
    private boolean ativo;

    @NotAudited
    @Version
    @Column(nullable = false)
    private Long versao;
}
