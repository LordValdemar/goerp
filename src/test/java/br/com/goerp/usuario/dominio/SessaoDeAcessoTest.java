package br.com.goerp.usuario.dominio;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import java.time.Instant;

import org.junit.jupiter.api.Test;

class SessaoDeAcessoTest {

    private static final Instant AGORA = Instant.parse("2026-11-20T12:00:00Z");
    private static final Duration SETE_DIAS = Duration.ofDays(7);

    @Test
    void validaAteExpirar() {
        SessaoDeAcesso sessao = SessaoDeAcesso.abrir(1L, 2L, "hash", AGORA, SETE_DIAS);

        assertThat(sessao.validaEm(AGORA.plus(SETE_DIAS).minusSeconds(1))).isTrue();
        assertThat(sessao.validaEm(AGORA.plus(SETE_DIAS))).isFalse();
    }

    @Test
    void sessaoRevogadaDeixaDeSerValida() {
        SessaoDeAcesso sessao = SessaoDeAcesso.abrir(1L, 2L, "hash", AGORA, SETE_DIAS);

        sessao.revogar(AGORA);

        assertThat(sessao.revogada()).isTrue();
        assertThat(sessao.validaEm(AGORA)).isFalse();
    }

    @Test
    void revogarDeNovoMantemAPrimeiraData() {
        SessaoDeAcesso sessao = SessaoDeAcesso.abrir(1L, 2L, "hash", AGORA, SETE_DIAS);

        sessao.revogar(AGORA);
        sessao.revogar(AGORA.plusSeconds(60));

        assertThat(sessao.revogadaEm()).isEqualTo(AGORA);
    }
}
