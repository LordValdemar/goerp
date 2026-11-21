package br.com.goerp.usuario.infraestrutura.seguranca;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import br.com.goerp.usuario.dominio.Senha;
import br.com.goerp.usuario.dominio.SenhaCriptografada;

class CodificadorDeSenhaBCryptTest {

    private final CodificadorDeSenhaBCrypt codificador = new CodificadorDeSenhaBCrypt();

    @Test
    void hashNaoContemASenhaEConfereComASenhaCerta() {
        SenhaCriptografada hash = codificador.criptografar(Senha.de("segredo123"));

        assertThat(hash.hash()).doesNotContain("segredo123").startsWith("$2a$12$");
        assertThat(codificador.confere("segredo123", hash)).isTrue();
    }

    @Test
    void naoConfereComSenhaErradaOuNula() {
        SenhaCriptografada hash = codificador.criptografar(Senha.de("segredo123"));

        assertThat(codificador.confere("segredo124", hash)).isFalse();
        assertThat(codificador.confere(null, hash)).isFalse();
    }

    @Test
    void mesmaSenhaGeraHashesDiferentes() {
        Senha senha = Senha.de("segredo123");

        assertThat(codificador.criptografar(senha)).isNotEqualTo(codificador.criptografar(senha));
    }
}
