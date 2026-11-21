package br.com.goerp.financeiro.dominio;

import static br.com.goerp.financeiro.dominio.FormaDePagamento.CARTAO_DEBITO;
import static br.com.goerp.financeiro.dominio.FormaDePagamento.DINHEIRO;
import static br.com.goerp.financeiro.dominio.TipoDeLancamento.ENTRADA;
import static br.com.goerp.financeiro.dominio.TipoDeLancamento.SAIDA;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;

import org.junit.jupiter.api.Test;

import br.com.goerp.compartilhado.dominio.Dinheiro;
import br.com.goerp.compartilhado.dominio.excecao.CampoObrigatorioException;
import br.com.goerp.compartilhado.dominio.excecao.ValorInvalidoException;

class ContaFinanceiraTest {

    private static final Instant AGORA = Instant.parse("2026-11-21T12:00:00Z");

    private final ContaFinanceira caixa = ContaFinanceira.reconstituir(1L, 10L, TipoDeConta.CAIXA, "Tesouraria", false,
            Dinheiro.ZERO, true, 0L);

    @Test
    void cadaLancamentoGuardaAFormaEOSaldoResultante() {
        caixa.lancar(pedido(ENTRADA, "150.00", DINHEIRO), AGORA);
        Lancamento saida = caixa.lancar(pedido(SAIDA, "40.10", CARTAO_DEBITO), AGORA);

        assertThat(saida.forma()).isEqualTo(CARTAO_DEBITO);
        assertThat(saida.saldoResultante()).isEqualTo(Dinheiro.de("109.90"));
        assertThat(saida.efeitoNoSaldo()).isEqualTo(Dinheiro.de("-40.10"));
        assertThat(caixa.saldo()).isEqualTo(Dinheiro.de("109.90"));
    }

    @Test
    void naoFicaNegativaSalvoSePermitido() {
        caixa.lancar(pedido(ENTRADA, "10", DINHEIRO), AGORA);

        assertThatThrownBy(() -> caixa.lancar(pedido(SAIDA, "10.01", DINHEIRO), AGORA))
                .isInstanceOf(SaldoInsuficienteException.class)
                .hasMessage("Saldo insuficiente na conta: disponível 10.00, pedido 10.01");
        assertThat(caixa.saldo()).isEqualTo(Dinheiro.de("10"));

        ContaFinanceira banco = ContaFinanceira.reconstituir(2L, 10L, TipoDeConta.BANCO, "Banco X", true, Dinheiro.ZERO,
                true, 0L);
        assertThat(banco.lancar(pedido(SAIDA, "500", FormaDePagamento.BOLETO), AGORA).saldoResultante())
                .isEqualTo(Dinheiro.de("-500"));
    }

    @Test
    void estornoDesfazComOSentidoOpostoEUmEstornoNaoSeEstorna() {
        Lancamento entrada = new Lancamento(5L, 10L, 1L, ENTRADA, Dinheiro.de("80"), Dinheiro.de("80"), DINHEIRO, 3L,
                "Venda", OrigemDoLancamento.VENDA, 99L, null, 7L, AGORA);
        caixa.lancar(pedido(ENTRADA, "80", DINHEIRO), AGORA);

        Lancamento estorno = caixa.lancar(entrada.pedidoDeEstorno("Venda cancelada", 7L), AGORA);

        assertThat(estorno.tipo()).isEqualTo(TipoDeLancamento.ESTORNO_DE_ENTRADA);
        assertThat(estorno.estornado()).isEqualTo(5L);
        assertThat(estorno.forma()).isEqualTo(DINHEIRO);
        assertThat(estorno.origem()).isEqualTo(OrigemDoLancamento.VENDA);
        assertThat(estorno.documentoDeOrigem()).isEqualTo(99L);
        assertThat(caixa.saldo()).isEqualTo(Dinheiro.ZERO);
        assertThatThrownBy(() -> estorno.pedidoDeEstorno("De novo", 7L)).isInstanceOf(EstornoDeEstornoException.class);
    }

    @Test
    void lancamentoExigeValorPositivoFormaQueMovimentaEDescricao() {
        assertThatThrownBy(() -> pedido(ENTRADA, "0", DINHEIRO)).isInstanceOf(ValorInvalidoException.class);
        assertThatThrownBy(() -> pedido(ENTRADA, "10", FormaDePagamento.A_PRAZO))
                .isInstanceOf(FormaSemMovimentoNaHoraException.class);
        assertThatThrownBy(() -> new PedidoDeLancamento(ENTRADA, Dinheiro.de("10"), DINHEIRO, null, " ", null, null,
                null, 7L)).isInstanceOf(CampoObrigatorioException.class);
        assertThatThrownBy(() -> new PedidoDeLancamento(ENTRADA, Dinheiro.de("10"), null, null, "x", null, null, null,
                7L)).isInstanceOf(NullPointerException.class);
    }

    @Test
    void soInativaZeradaEInativaNaoRecebeLancamento() {
        caixa.lancar(pedido(ENTRADA, "1", DINHEIRO), AGORA);
        assertThatThrownBy(caixa::inativar).isInstanceOf(ContaComSaldoException.class);

        caixa.lancar(pedido(SAIDA, "1", DINHEIRO), AGORA);
        caixa.inativar();

        assertThatThrownBy(() -> caixa.lancar(pedido(ENTRADA, "1", DINHEIRO), AGORA))
                .isInstanceOf(ContaFinanceiraInativaException.class);
        caixa.reativar();
        assertThat(caixa.ativa()).isTrue();
    }

    @Test
    void contaNovaNasceZeradaEExigeNome() {
        assertThat(ContaFinanceira.nova(10L, TipoDeConta.CONTA_DE_PAGAMENTO, "Maquininha", false).saldo())
                .isEqualTo(Dinheiro.ZERO);
        assertThatThrownBy(() -> ContaFinanceira.nova(10L, TipoDeConta.BANCO, "", false))
                .isInstanceOf(CampoObrigatorioException.class);
    }

    private static PedidoDeLancamento pedido(TipoDeLancamento tipo, String valor, FormaDePagamento forma) {
        return new PedidoDeLancamento(tipo, Dinheiro.de(valor), forma, null, "Lançamento", null, null, null, 7L);
    }
}
