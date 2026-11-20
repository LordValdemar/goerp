package br.com.goerp.usuario.dominio;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import java.time.Instant;

import org.junit.jupiter.api.Test;

import br.com.goerp.compartilhado.dominio.Email;

class ControleDeAcessoTest {

    private static final Instant AGORA = Instant.parse("2026-11-20T12:00:00Z");

    @Test
    void bloqueiaNaQuintaFalhaSeguida() {
        Usuario usuario = novoUsuario();

        for (int i = 1; i < ControleDeAcesso.MAXIMO_DE_TENTATIVAS; i++) {
            usuario.registrarFalhaDeLogin(AGORA);
            assertThat(usuario.estaBloqueado(AGORA)).as("falha %d", i).isFalse();
        }
        usuario.registrarFalhaDeLogin(AGORA);

        assertThat(usuario.estaBloqueado(AGORA)).isTrue();
    }

    @Test
    void bloqueioTerminaDepoisDe15Minutos() {
        Usuario usuario = bloqueado();

        assertThat(usuario.estaBloqueado(AGORA.plus(Duration.ofMinutes(14)))).isTrue();
        assertThat(usuario.estaBloqueado(AGORA.plus(Duration.ofMinutes(15)))).isFalse();
    }

    @Test
    void loginComSucessoZeraAsFalhas() {
        Usuario usuario = novoUsuario();
        usuario.registrarFalhaDeLogin(AGORA);
        usuario.registrarFalhaDeLogin(AGORA);

        usuario.registrarLoginComSucesso();

        assertThat(usuario.controleDeAcesso()).isEqualTo(ControleDeAcesso.livre());
    }

    @Test
    void depoisDoBloqueioAContagemRecomecaDoZero() {
        Usuario usuario = bloqueado();
        Instant depois = AGORA.plus(ControleDeAcesso.DURACAO_DO_BLOQUEIO);

        usuario.registrarFalhaDeLogin(depois);

        assertThat(usuario.estaBloqueado(depois)).isFalse();
        assertThat(usuario.controleDeAcesso().tentativasFalhas()).isEqualTo(1);
    }

    private static Usuario bloqueado() {
        Usuario usuario = novoUsuario();
        for (int i = 0; i < ControleDeAcesso.MAXIMO_DE_TENTATIVAS; i++) {
            usuario.registrarFalhaDeLogin(AGORA);
        }
        return usuario;
    }

    private static Usuario novoUsuario() {
        return Usuario.novo("Maria", Email.de("maria@exemplo.com"), new SenhaCriptografada("hash"));
    }
}
