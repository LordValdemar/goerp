package br.com.goerp.vendas.aplicacao;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import br.com.goerp.compartilhado.dominio.Dinheiro;
import br.com.goerp.compartilhado.dominio.excecao.OperacaoNaoPermitidaException;
import br.com.goerp.vendas.dominio.Caixa;
import br.com.goerp.vendas.dominio.CaixaRepositorio;
import br.com.goerp.financeiro.dominio.FormaDePagamento;
import br.com.goerp.vendas.dominio.SituacaoDoCaixa;

class CaixasDaEmpresaTest {

    private static final Long OPERADOR = 7L;
    private static final Long OUTRO = 8L;

    CaixaRepositorio caixas = mock(CaixaRepositorio.class);

    @Test
    void operadorAcessaOProprioCaixa() {
        assertThat(caixasComo(OPERADOR, false).buscarComAcesso(1L).operadorId()).isEqualTo(OPERADOR);
    }

    @Test
    void outroOperadorSoComPermissaoDeGerente() {
        assertThatThrownBy(() -> caixasComo(OUTRO, false).buscarComAcesso(1L))
                .isInstanceOf(OperacaoNaoPermitidaException.class);
        assertThat(caixasComo(OUTRO, true).buscarComAcesso(1L)).isNotNull();
    }

    @Test
    void resumoTrazOEsperadoDeTodasAsFormas() {
        var resumo = caixasComo(OPERADOR, false).resumir(caixa());

        assertThat(resumo.esperado()).containsOnlyKeys(FormaDePagamento.conferidasNoCaixa())
                .containsEntry(FormaDePagamento.DINHEIRO, Dinheiro.de("100"))
                .containsEntry(FormaDePagamento.PIX, Dinheiro.ZERO);
    }

    private CaixasDaEmpresa caixasComo(Long usuario, boolean gerente) {
        when(caixas.buscarPorId(1L)).thenReturn(Optional.of(caixa()));
        return new CaixasDaEmpresa(caixas, caixaId -> Map.of(), () -> usuario, permissao -> gerente);
    }

    private static Caixa caixa() {
        return Caixa.reconstituir(1L, 1L, OPERADOR, Dinheiro.de("100"), Instant.now(), java.util.List.of(),
                SituacaoDoCaixa.ABERTO, null, null, java.util.List.of());
    }
}
