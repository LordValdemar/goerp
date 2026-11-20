package br.com.goerp.usuario.dominio;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import br.com.goerp.compartilhado.dominio.excecao.CampoObrigatorioException;

class SenhaTest {

    @Test
    void aceitaSenhaComLetrasENumerosE8Caracteres() {
        assertThat(Senha.de("abcdef12").valor()).isEqualTo("abcdef12");
    }

    @ParameterizedTest
    @ValueSource(strings = {"abc1234", "abcdefgh", "12345678"})
    void rejeitaSenhaCurtaSemLetraOuSemNumero(String valor) {
        assertThatThrownBy(() -> Senha.de(valor)).isInstanceOf(SenhaFracaException.class);
    }

    @Test
    void rejeitaSenhaMaiorQueOLimiteDoBCrypt() {
        assertThatThrownBy(() -> Senha.de("a1".repeat(37))).isInstanceOf(SenhaFracaException.class);
    }

    @Test
    void exigeSenha() {
        assertThatThrownBy(() -> Senha.de("")).isInstanceOf(CampoObrigatorioException.class);
    }

    @Test
    void naoExpoeASenhaNoToString() {
        assertThat(Senha.de("segredo123").toString()).doesNotContain("segredo123");
        assertThat(new SenhaCriptografada("$2a$hash").toString()).doesNotContain("hash");
    }
}
