package br.com.goerp.usuario.aplicacao.autenticacao;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class TokenDeRenovacaoTest {

    @Test
    void geraTokensDiferentesELongos() {
        String primeiro = TokenDeRenovacao.gerar();

        assertThat(primeiro).hasSizeGreaterThanOrEqualTo(43).isNotEqualTo(TokenDeRenovacao.gerar());
    }

    @Test
    void hashEhDeterministicoEDiferenteDoToken() {
        String token = TokenDeRenovacao.gerar();

        assertThat(TokenDeRenovacao.hash(token)).isEqualTo(TokenDeRenovacao.hash(token)).isNotEqualTo(token).hasSize(64);
    }
}
