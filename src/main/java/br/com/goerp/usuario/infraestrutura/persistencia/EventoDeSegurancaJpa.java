package br.com.goerp.usuario.infraestrutura.persistencia;

import java.time.Instant;

import br.com.goerp.usuario.dominio.TipoDeEventoDeSeguranca;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/** Registro de auditoria em si: só é inserido, nunca alterado. */
@Getter
@Setter
@Entity(name = "EventoDeSeguranca")
@Table(name = "evento_seguranca")
class EventoDeSegurancaJpa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, updatable = false)
    private TipoDeEventoDeSeguranca tipo;

    @Column(name = "email_informado", updatable = false)
    private String emailInformado;

    @Column(name = "usuario_id", updatable = false)
    private Long usuarioId;

    @Column(name = "empresa_id", updatable = false)
    private Long empresaId;

    @Column(updatable = false)
    private String ip;

    @Column(nullable = false, updatable = false)
    private Instant momento;
}
