package br.com.goerp.cadastros.cliente.infraestrutura.persistencia;

import org.hibernate.annotations.TenantId;
import org.hibernate.envers.Audited;
import org.hibernate.envers.NotAudited;

import br.com.goerp.cadastros.cliente.dominio.IndicadorDeIe;
import br.com.goerp.compartilhado.dominio.Dinheiro;
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
@Entity(name = "Cliente")
@Table(name = "cliente")
class ClienteJpa extends EntidadeJpaAuditavel {

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

    @Enumerated(EnumType.STRING)
    @Column(name = "indicador_ie", nullable = false)
    private IndicadorDeIe indicadorDeIe;

    private Email email;

    private Telefone telefone;

    @Embedded
    private EnderecoJpa endereco;

    @Column(name = "limite_credito")
    private Dinheiro limiteDeCredito;

    private String observacoes;

    @Column(nullable = false)
    private boolean ativo;

    @NotAudited
    @Version
    @Column(nullable = false)
    private Long versao;
}
