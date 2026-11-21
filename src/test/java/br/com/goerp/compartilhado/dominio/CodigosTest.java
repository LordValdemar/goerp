package br.com.goerp.compartilhado.dominio;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.api.Test;

import br.com.goerp.compartilhado.dominio.excecao.GtinInvalidoException;
import br.com.goerp.compartilhado.dominio.excecao.NcmInvalidoException;
import br.com.goerp.compartilhado.dominio.excecao.TelefoneInvalidoException;

class CodigosTest {

    @ParameterizedTest
    @ValueSource(strings = {"7891000315507", "96385074", "012345678905", "17891000315504"})
    void aceitaGtinsValidosDe8a14Digitos(String valor) {
        assertThat(Gtin.de(valor).valor()).isEqualTo(valor);
    }

    @ParameterizedTest
    @ValueSource(strings = {"7891000315508", "789100031550", "123", "78910003155071"})
    void rejeitaGtinComDigitoOuTamanhoErrado(String valor) {
        assertThatThrownBy(() -> Gtin.de(valor)).isInstanceOf(GtinInvalidoException.class);
    }

    @Test
    void ncmTemOitoDigitos() {
        assertThat(Ncm.de("2202.10.00").valor()).isEqualTo("22021000");
        assertThatThrownBy(() -> Ncm.de("2202100")).isInstanceOf(NcmInvalidoException.class);
    }

    @Test
    void telefoneFixoECelular() {
        assertThat(Telefone.de("(11) 3333-4444").formatado()).isEqualTo("(11) 3333-4444");
        assertThat(Telefone.de("11 98888-7777").formatado()).isEqualTo("(11) 98888-7777");
        assertThatThrownBy(() -> Telefone.de("3333-4444")).isInstanceOf(TelefoneInvalidoException.class);
    }
}
