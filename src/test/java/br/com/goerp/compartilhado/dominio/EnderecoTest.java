package br.com.goerp.compartilhado.dominio;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

import br.com.goerp.compartilhado.dominio.excecao.CampoObrigatorioException;
import br.com.goerp.compartilhado.dominio.excecao.CepInvalidoException;
import br.com.goerp.compartilhado.dominio.excecao.CodigoIbgeInvalidoException;
import br.com.goerp.compartilhado.dominio.excecao.ValorInvalidoException;

class EnderecoTest {

    private static final String SAO_PAULO = "3550308";

    @Test
    void enderecoCompleto() {
        Endereco endereco = new Endereco(Cep.de("01310-100"), "Av. Paulista", "1000", " ", "Bela Vista", "São Paulo",
                SAO_PAULO, Uf.de("sp"));

        assertThat(endereco.cep().formatado()).isEqualTo("01310-100");
        assertThat(endereco.complemento()).isNull();
        assertThat(endereco.uf()).isEqualTo(Uf.SP);
    }

    @Test
    void semNumeroViraSN() {
        Endereco endereco = new Endereco(Cep.de("01310100"), "Rua A", null, null, "Centro", "São Paulo", SAO_PAULO, Uf.SP);

        assertThat(endereco.numero()).isEqualTo("S/N");
    }

    @Test
    void codigoIbgePrecisaSerDaUf() {
        assertThatThrownBy(() -> new Endereco(Cep.de("01310100"), "Rua A", "1", null, "Centro", "São Paulo", SAO_PAULO,
                Uf.RJ)).isInstanceOf(CodigoIbgeInvalidoException.class);
        assertThatThrownBy(() -> new Endereco(Cep.de("01310100"), "Rua A", "1", null, "Centro", "São Paulo", "355030",
                Uf.SP)).isInstanceOf(CodigoIbgeInvalidoException.class);
    }

    @Test
    void exigeCamposObrigatorios() {
        assertThatThrownBy(() -> new Endereco(Cep.de("01310100"), " ", "1", null, "Centro", "São Paulo", SAO_PAULO,
                Uf.SP)).isInstanceOf(CampoObrigatorioException.class).hasMessageContaining("logradouro");
    }

    @Test
    void cepEUfInvalidos() {
        assertThatThrownBy(() -> Cep.de("0131010")).isInstanceOf(CepInvalidoException.class);
        assertThatThrownBy(() -> Uf.de("XX")).isInstanceOf(ValorInvalidoException.class);
    }
}
