package br.com.goerp.cadastros.cliente.dominio;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

import br.com.goerp.compartilhado.dominio.Dinheiro;
import br.com.goerp.compartilhado.dominio.Documento;
import br.com.goerp.compartilhado.dominio.TipoDePessoa;
import br.com.goerp.compartilhado.dominio.excecao.CampoObrigatorioException;
import br.com.goerp.compartilhado.dominio.excecao.ValorInvalidoException;

class ClienteTest {

    @Test
    void novoClienteNasceAtivoComTipoDePessoaDoDocumento() {
        Cliente pessoaFisica = Cliente.novo(1L, Documento.de("529.982.247-25"), dados("Maria"));
        Cliente pessoaJuridica = Cliente.novo(1L, Documento.de("11.222.333/0001-81"), dados("Loja LTDA"));

        assertThat(pessoaFisica.ativo()).isTrue();
        assertThat(pessoaFisica.tipoDePessoa()).isEqualTo(TipoDePessoa.FISICA);
        assertThat(pessoaJuridica.tipoDePessoa()).isEqualTo(TipoDePessoa.JURIDICA);
    }

    @Test
    void semIndicadorEhNaoContribuinte() {
        assertThat(dados("Maria").indicadorDeIe()).isEqualTo(IndicadorDeIe.NAO_CONTRIBUINTE);
    }

    @Test
    void contribuinteExigeInscricaoEstadual() {
        assertThatThrownBy(() -> new DadosDoCliente("Loja", null, " ", IndicadorDeIe.CONTRIBUINTE, null, null, null,
                null, null)).isInstanceOf(InscricaoEstadualException.class);

        assertThat(new DadosDoCliente("Loja", null, "123456789", IndicadorDeIe.CONTRIBUINTE, null, null, null, null,
                null).inscricaoEstadual()).isEqualTo("123456789");
    }

    @Test
    void isentoENaoContribuinteNaoInformamInscricao() {
        assertThatThrownBy(() -> new DadosDoCliente("Loja", null, "123456789", IndicadorDeIe.ISENTO, null, null, null,
                null, null)).isInstanceOf(InscricaoEstadualException.class);
    }

    @Test
    void exigeNomeERecusaLimiteNegativo() {
        assertThatThrownBy(() -> dados(" ")).isInstanceOf(CampoObrigatorioException.class);
        assertThatThrownBy(() -> new DadosDoCliente("Maria", null, null, null, null, null, null, Dinheiro.de("-10"),
                null)).isInstanceOf(ValorInvalidoException.class);
    }

    @Test
    void alteraInativaEReativa() {
        Cliente cliente = Cliente.novo(1L, Documento.de("52998224725"), dados("Maria"));

        cliente.alterar(dados("Maria Souza"));
        cliente.inativar();
        assertThat(cliente.dados().nome()).isEqualTo("Maria Souza");
        assertThat(cliente.ativo()).isFalse();

        cliente.ativar();
        assertThat(cliente.ativo()).isTrue();
    }

    static DadosDoCliente dados(String nome) {
        return new DadosDoCliente(nome, null, null, null, null, null, null, null, null);
    }
}
