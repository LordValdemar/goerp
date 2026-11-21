package br.com.goerp.compartilhado.infraestrutura.seguranca;

import java.util.Optional;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;

/** Lê, do token JWT da requisição atual, quem é o usuário e em qual empresa ele está. */
public final class UsuarioAutenticado {

    public static final String CLAIM_EMPRESA = "empresa";
    public static final String CLAIM_PERMISSOES = "permissoes";
    public static final String CLAIM_NOME = "nome";

    private UsuarioAutenticado() {
    }

    public static Optional<Long> idUsuario() {
        return tokenAtual().map(jwt -> Long.valueOf(jwt.getSubject()));
    }

    public static Optional<Long> idEmpresa() {
        return tokenAtual()
                .map(jwt -> jwt.<Number>getClaim(CLAIM_EMPRESA))
                .map(Number::longValue);
    }

    private static Optional<Jwt> tokenAtual() {
        Authentication autenticacao = SecurityContextHolder.getContext().getAuthentication();
        if (autenticacao != null && autenticacao.getPrincipal() instanceof Jwt jwt) {
            return Optional.of(jwt);
        }
        return Optional.empty();
    }
}
