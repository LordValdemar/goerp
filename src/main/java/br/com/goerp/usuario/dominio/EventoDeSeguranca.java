package br.com.goerp.usuario.dominio;

import java.time.Instant;
import java.util.Objects;

/** Registro permanente de um acontecimento de segurança (login, falha, bloqueio, logout...). */
public record EventoDeSeguranca(
        TipoDeEventoDeSeguranca tipo,
        String emailInformado,
        Long usuarioId,
        Long empresaId,
        Instant momento) {

    public EventoDeSeguranca {
        Objects.requireNonNull(tipo, "tipo");
        Objects.requireNonNull(momento, "momento");
    }

    public static EventoDeSeguranca doUsuario(TipoDeEventoDeSeguranca tipo, Usuario usuario, Long empresaId,
            Instant momento) {
        return new EventoDeSeguranca(tipo, usuario.email().valor(), usuario.id(), empresaId, momento);
    }

    public static EventoDeSeguranca deEmailDesconhecido(String emailInformado, Instant momento) {
        return new EventoDeSeguranca(TipoDeEventoDeSeguranca.LOGIN_FALHA, emailInformado, null, null, momento);
    }
}
