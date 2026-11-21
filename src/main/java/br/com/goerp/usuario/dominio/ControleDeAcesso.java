package br.com.goerp.usuario.dominio;

import java.time.Duration;
import java.time.Instant;

/**
 * Proteção contra tentativa e erro de senhas: após {@value #MAXIMO_DE_TENTATIVAS} falhas
 * seguidas, o login fica bloqueado por {@link #DURACAO_DO_BLOQUEIO}.
 */
public record ControleDeAcesso(int tentativasFalhas, Instant bloqueadoAte) {

    public static final int MAXIMO_DE_TENTATIVAS = 5;
    public static final Duration DURACAO_DO_BLOQUEIO = Duration.ofMinutes(15);

    public static ControleDeAcesso livre() {
        return new ControleDeAcesso(0, null);
    }

    public boolean bloqueadoEm(Instant agora) {
        return bloqueadoAte != null && agora.isBefore(bloqueadoAte);
    }

    public ControleDeAcesso comFalhaEm(Instant agora) {
        int tentativas = tentativasFalhas + 1;
        if (tentativas >= MAXIMO_DE_TENTATIVAS) {
            return new ControleDeAcesso(0, agora.plus(DURACAO_DO_BLOQUEIO));
        }
        return new ControleDeAcesso(tentativas, null);
    }
}
