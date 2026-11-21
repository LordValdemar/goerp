package br.com.goerp.vendas.dominio;

import static br.com.goerp.vendas.dominio.FormaDePagamento.CARTAO_DEBITO;
import static br.com.goerp.vendas.dominio.FormaDePagamento.DINHEIRO;
import static br.com.goerp.vendas.dominio.FormaDePagamento.PIX;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import br.com.goerp.compartilhado.dominio.Dinheiro;
import br.com.goerp.compartilhado.dominio.excecao.CampoObrigatorioException;
import br.com.goerp.compartilhado.dominio.excecao.ValorInvalidoException;

class CaixaTest {

    private static final Instant AGORA = Instant.parse("2026-11-21T08:00:00Z");
    private static final Map<FormaDePagamento, Dinheiro> VENDAS = Map.of(
            DINHEIRO, Dinheiro.de("150"), CARTAO_DEBITO, Dinheiro.de("80"), PIX, Dinheiro.de("45.50"));

    private final Caixa caixa = Caixa.abrir(1L, 7L, Dinheiro.de("100"), AGORA);

    @Test
    void dinheiroEsperadoSomaTrocoSuprimentoVendasMenosSangria() {
        caixa.registrarSuprimento(Dinheiro.de("50"), null, 7L, AGORA);
        caixa.registrarSangria(Dinheiro.de("200"), "Cofre", 7L, AGORA, VENDAS);

        // 100 + 50 - 200 + 150 = 100
        assertThat(caixa.esperadoEm(DINHEIRO, VENDAS)).isEqualTo(Dinheiro.de("100"));
        assertThat(caixa.esperadoEm(CARTAO_DEBITO, VENDAS)).isEqualTo(Dinheiro.de("80"));
    }

    @Test
    void fechamentoConfereCadaFormaERegistraSobraEFalta() {
        List<ConferenciaDeCaixa> conferencias = caixa.fechar(Map.of(
                DINHEIRO, Dinheiro.de("245"), CARTAO_DEBITO, Dinheiro.de("80"), PIX, Dinheiro.de("46.50")),
                VENDAS, 7L, AGORA);

        assertThat(conferencias).hasSize(FormaDePagamento.values().length);
        assertThat(conferencia(conferencias, DINHEIRO).diferenca()).isEqualTo(Dinheiro.de("-5"));
        assertThat(conferencia(conferencias, CARTAO_DEBITO).confere()).isTrue();
        assertThat(conferencia(conferencias, PIX).diferenca()).isEqualTo(Dinheiro.de("1"));
        assertThat(conferencia(conferencias, FormaDePagamento.CARTAO_CREDITO).confere()).isTrue();
        assertThat(caixa.situacao()).isEqualTo(SituacaoDoCaixa.FECHADO);
    }

    @Test
    void sangriaPrecisaDeMotivoENaoPassaDoDinheiroDisponivel() {
        assertThatThrownBy(() -> caixa.registrarSangria(Dinheiro.de("10"), " ", 7L, AGORA, VENDAS))
                .isInstanceOf(CampoObrigatorioException.class);
        assertThatThrownBy(() -> caixa.registrarSangria(Dinheiro.de("250.01"), "Cofre", 7L, AGORA, VENDAS))
                .isInstanceOf(SangriaMaiorQueODinheiroException.class);

        caixa.registrarSangria(Dinheiro.de("250"), "Cofre", 7L, AGORA, VENDAS);
        assertThat(caixa.esperadoEm(DINHEIRO, VENDAS)).isEqualTo(Dinheiro.ZERO);
    }

    @Test
    void caixaFechadoNaoAceitaMaisNada() {
        caixa.fechar(Map.of(), Map.of(), 7L, AGORA);

        assertThatThrownBy(() -> caixa.registrarSuprimento(Dinheiro.de("1"), null, 7L, AGORA))
                .isInstanceOf(CaixaFechadoException.class);
        assertThatThrownBy(() -> caixa.fechar(Map.of(), Map.of(), 7L, AGORA)).isInstanceOf(CaixaFechadoException.class);
        assertThatThrownBy(caixa::exigirAberto).isInstanceOf(CaixaFechadoException.class);
    }

    @Test
    void valoresInvalidos() {
        assertThatThrownBy(() -> Caixa.abrir(1L, 7L, Dinheiro.de("-1"), AGORA)).isInstanceOf(ValorInvalidoException.class);
        assertThatThrownBy(() -> caixa.registrarSuprimento(Dinheiro.ZERO, null, 7L, AGORA))
                .isInstanceOf(ValorInvalidoException.class);
    }

    private static ConferenciaDeCaixa conferencia(List<ConferenciaDeCaixa> conferencias, FormaDePagamento forma) {
        return conferencias.stream().filter(c -> c.forma() == forma).findFirst().orElseThrow();
    }
}
