package br.com.goerp.compartilhado.dominio;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import br.com.goerp.compartilhado.dominio.excecao.CampoObrigatorioException;
import br.com.goerp.compartilhado.dominio.excecao.EmailInvalidoException;

class EmailTest {

    @Test
    void normalizaParaMinusculoESemEspacos() {
        assertThat(Email.de("  Maria.Silva@Exemplo.COM ").valor()).isEqualTo("maria.silva@exemplo.com");
    }

    @Test
    void emailsQueDiferemSoNasMaiusculasSaoIguais() {
        assertThat(Email.de("MARIA@exemplo.com")).isEqualTo(Email.de("maria@exemplo.com"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"maria", "maria@", "@exemplo.com", "maria@exemplo", "ma ria@exemplo.com", "a@b@c.com"})
    void rejeitaFormatoInvalido(String valor) {
        assertThatThrownBy(() -> Email.de(valor)).isInstanceOf(EmailInvalidoException.class);
    }

    @Test
    void rejeitaEmailMaiorQue150Caracteres() {
        String longo = "a".repeat(140) + "@exemplo.com";

        assertThatThrownBy(() -> Email.de(longo)).isInstanceOf(EmailInvalidoException.class);
    }

    @ParameterizedTest
    @NullAndEmptySource
    void exigeEmail(String valor) {
        assertThatThrownBy(() -> Email.de(valor)).isInstanceOf(CampoObrigatorioException.class);
    }
}
