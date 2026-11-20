package br.com.goerp.compartilhado.dominio;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import br.com.goerp.compartilhado.dominio.excecao.CampoObrigatorioException;
import br.com.goerp.compartilhado.dominio.excecao.CnpjInvalidoException;

class CnpjTest {

    @Test
    void aceitaCnpjValidoComMascaraEGuardaApenasDigitos() {
        Cnpj cnpj = Cnpj.de("11.222.333/0001-81");

        assertThat(cnpj.valor()).isEqualTo("11222333000181");
        assertThat(cnpj.formatado()).isEqualTo("11.222.333/0001-81");
    }

    @ParameterizedTest
    @ValueSource(strings = {"11222333000182", "1122233300018", "112223330001811", "11111111111111", "abc"})
    void rejeitaCnpjInvalido(String valor) {
        assertThatThrownBy(() -> Cnpj.de(valor)).isInstanceOf(CnpjInvalidoException.class);
    }

    @ParameterizedTest
    @NullAndEmptySource
    void exigeCnpj(String valor) {
        assertThatThrownBy(() -> Cnpj.de(valor)).isInstanceOf(CampoObrigatorioException.class);
    }

    @Test
    void cnpjsComMesmosDigitosSaoIguais() {
        assertThat(Cnpj.de("11.222.333/0001-81")).isEqualTo(Cnpj.de("11222333000181"));
    }
}
