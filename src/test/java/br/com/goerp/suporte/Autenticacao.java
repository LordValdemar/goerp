package br.com.goerp.suporte;

import java.time.Instant;
import java.util.List;
import java.util.function.Supplier;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

/** Simula, dentro de um teste, um usuário autenticado com um token como o emitido no login. */
public final class Autenticacao {

    private Autenticacao() {
    }

    public static <T> T comoUsuarioDaEmpresa(Long empresaId, Supplier<T> acao) {
        return comToken(token(empresaId), acao);
    }

    /** Um usuário específico (ex.: o operador dono do caixa) dentro da empresa. */
    public static <T> T comoUsuario(Long usuarioId, Long empresaId, Supplier<T> acao) {
        Jwt token = Jwt.withTokenValue("teste").header("alg", "HS256").subject(usuarioId.toString())
                .claim("empresa", empresaId).issuedAt(Instant.now()).expiresAt(Instant.now().plusSeconds(60)).build();
        return comToken(token, acao);
    }

    public static <T> T comoAdministradorDaPlataforma(Supplier<T> acao) {
        return comToken(token(null), acao);
    }

    private static <T> T comToken(Jwt token, Supplier<T> acao) {
        SecurityContextHolder.getContext().setAuthentication(new JwtAuthenticationToken(token, List.of()));
        try {
            return acao.get();
        } finally {
            SecurityContextHolder.clearContext();
        }
    }

    private static Jwt token(Long empresaId) {
        Jwt.Builder token = Jwt.withTokenValue("teste")
                .header("alg", "HS256")
                .subject("1")
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(60));
        if (empresaId != null) {
            token.claim("empresa", empresaId);
        }
        return token.build();
    }
}
