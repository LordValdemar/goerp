package br.com.goerp.compartilhado.dominio;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import br.com.goerp.compartilhado.dominio.excecao.CampoObrigatorioException;
import br.com.goerp.compartilhado.dominio.excecao.CpfInvalidoException;
import br.com.goerp.compartilhado.dominio.excecao.DocumentoInvalidoException;

class DocumentoTest {

    @Test
    void cpfValidoComMascara() {
        Cpf cpf = Cpf.de("529.982.247-25");

        assertThat(cpf.valor()).isEqualTo("52998224725");
        assertThat(cpf.formatado()).isEqualTo("529.982.247-25");
        assertThat(cpf.tipoDePessoa()).isEqualTo(TipoDePessoa.FISICA);
    }

    @ParameterizedTest
    @ValueSource(strings = {"52998224726", "5299822472", "11111111111"})
    void rejeitaCpfInvalido(String valor) {
        assertThatThrownBy(() -> Cpf.de(valor)).isInstanceOf(CpfInvalidoException.class);
    }

    @Test
    void documentoDescobreSeEhCpfOuCnpj() {
        assertThat(Documento.de("529.982.247-25")).isInstanceOf(Cpf.class);
        assertThat(Documento.de("11.222.333/0001-81"))
                .isInstanceOf(Cnpj.class)
                .extracting(Documento::tipoDePessoa).isEqualTo(TipoDePessoa.JURIDICA);
    }

    @Test
    void documentoComTamanhoErradoOuVazio() {
        assertThatThrownBy(() -> Documento.de("123456")).isInstanceOf(DocumentoInvalidoException.class);
        assertThatThrownBy(() -> Documento.de(" ")).isInstanceOf(CampoObrigatorioException.class);
    }
}
