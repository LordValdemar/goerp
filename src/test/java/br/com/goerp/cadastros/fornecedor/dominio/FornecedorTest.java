package br.com.goerp.cadastros.fornecedor.dominio;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

import br.com.goerp.compartilhado.dominio.Documento;
import br.com.goerp.compartilhado.dominio.TipoDePessoa;
import br.com.goerp.compartilhado.dominio.excecao.CampoObrigatorioException;

class FornecedorTest {

    @Test
    void novoFornecedorNasceAtivoEComTipoDePessoaDoDocumento() {
        Fornecedor fornecedor = Fornecedor.novo(1L, Documento.de("11.222.333/0001-81"), dados("Distribuidora"));

        assertThat(fornecedor.ativo()).isTrue();
        assertThat(fornecedor.tipoDePessoa()).isEqualTo(TipoDePessoa.JURIDICA);
    }

    @Test
    void exigeNomeELimpaCamposOpcionaisEmBranco() {
        assertThatThrownBy(() -> dados(" ")).isInstanceOf(CampoObrigatorioException.class);

        DadosDoFornecedor dados = new DadosDoFornecedor("Distribuidora", " ", " ", null, null, " ", null, " ");
        assertThat(dados.nomeFantasia()).isNull();
        assertThat(dados.pessoaDeContato()).isNull();
    }

    @Test
    void alteraInativaEReativa() {
        Fornecedor fornecedor = Fornecedor.novo(1L, Documento.de("11222333000181"), dados("Distribuidora"));

        fornecedor.alterar(dados("Distribuidora Nova"));
        fornecedor.inativar();
        assertThat(fornecedor.dados().nome()).isEqualTo("Distribuidora Nova");
        assertThat(fornecedor.ativo()).isFalse();

        fornecedor.ativar();
        assertThat(fornecedor.ativo()).isTrue();
    }

    private static DadosDoFornecedor dados(String nome) {
        return new DadosDoFornecedor(nome, null, null, null, null, null, null, null);
    }
}
