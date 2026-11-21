package br.com.goerp.empresa.dominio;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

import br.com.goerp.compartilhado.dominio.Cnpj;
import br.com.goerp.compartilhado.dominio.excecao.CampoObrigatorioException;

class EmpresaTest {

    private static final Cnpj CNPJ = Cnpj.de("11222333000181");

    @Test
    void novaEmpresaNasceAtivaESemId() {
        Empresa empresa = Empresa.nova(CNPJ, " Loja Exemplo LTDA ", "Loja Exemplo");

        assertThat(empresa.id()).isNull();
        assertThat(empresa.ativa()).isTrue();
        assertThat(empresa.razaoSocial()).isEqualTo("Loja Exemplo LTDA");
    }

    @Test
    void exigeRazaoSocial() {
        assertThatThrownBy(() -> Empresa.nova(CNPJ, "  ", null))
                .isInstanceOf(CampoObrigatorioException.class)
                .hasMessageContaining("razaoSocial");
    }

    @Test
    void nomeFantasiaEmBrancoViraNulo() {
        Empresa empresa = Empresa.nova(CNPJ, "Loja Exemplo LTDA", " ");

        assertThat(empresa.nomeFantasia()).isNull();
    }

    @Test
    void inativaEReativa() {
        Empresa empresa = Empresa.nova(CNPJ, "Loja Exemplo LTDA", null);

        empresa.inativar();
        assertThat(empresa.ativa()).isFalse();

        empresa.ativar();
        assertThat(empresa.ativa()).isTrue();
    }
}
