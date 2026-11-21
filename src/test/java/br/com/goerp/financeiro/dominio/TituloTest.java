package br.com.goerp.financeiro.dominio;

import static br.com.goerp.financeiro.dominio.FormaDePagamento.DINHEIRO;
import static br.com.goerp.financeiro.dominio.FormaDePagamento.PIX;
import static br.com.goerp.financeiro.dominio.SituacaoDoTitulo.ABERTO;
import static br.com.goerp.financeiro.dominio.SituacaoDoTitulo.CANCELADO;
import static br.com.goerp.financeiro.dominio.SituacaoDoTitulo.PARCIALMENTE_PAGO;
import static br.com.goerp.financeiro.dominio.SituacaoDoTitulo.QUITADO;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;

import br.com.goerp.compartilhado.dominio.Dinheiro;
import br.com.goerp.compartilhado.dominio.excecao.CampoObrigatorioException;
import br.com.goerp.compartilhado.dominio.excecao.ValorInvalidoException;

class TituloTest {

    private static final Instant AGORA = Instant.parse("2026-11-21T12:00:00Z");
    private static final LocalDate VENCIMENTO = LocalDate.of(2026, 12, 10);
    private static final DadosDoTitulo CREDIARIO = new DadosDoTitulo(NaturezaDoTitulo.A_RECEBER, 3L, "Venda 15", null,
            null, null, OrigemDoTitulo.VENDA, 15L);

    @Test
    void parcelasViramTitulosComOsMesmosDadosMarcadosAPrazo() {
        List<Titulo> titulos = Titulo.parcelas(1L, CREDIARIO, Parcelamento.mensal(Dinheiro.de("300"), 3, VENCIMENTO));

        assertThat(titulos).hasSize(3);
        assertThat(titulos).allSatisfy(t -> {
            assertThat(t.formaPrevista()).isEqualTo(FormaDePagamento.A_PRAZO);
            assertThat(t.situacao()).isEqualTo(ABERTO);
            assertThat(t.documentoDeOrigem()).isEqualTo(15L);
        });
        assertThat(titulos.get(1).parcela()).isEqualTo(2);
        assertThat(titulos.get(1).vencimento()).isEqualTo(VENCIMENTO.plusMonths(1));
    }

    @Test
    void pagamentoParcialEDepoisOResto() {
        Titulo titulo = titulo("100");

        titulo.baixar(baixa("40", "0", "0", "0", PIX), AGORA);
        assertThat(titulo.situacao()).isEqualTo(PARCIALMENTE_PAGO);
        assertThat(titulo.saldoDevedor()).isEqualTo(Dinheiro.de("60"));

        Baixa segunda = titulo.baixar(baixa("60", "0", "0", "0", DINHEIRO), AGORA);
        assertThat(segunda.numero()).isEqualTo(2);
        assertThat(segunda.forma()).isEqualTo(DINHEIRO);
        assertThat(titulo.situacao()).isEqualTo(QUITADO);
        assertThat(titulo.saldoDevedor()).isEqualTo(Dinheiro.ZERO);
    }

    @Test
    void jurosEMultaNaoAbatemDescontoAbate() {
        Titulo atrasado = titulo("100");
        // pagou 105,50 = 100 + 3,50 de juros + 2 de multa
        atrasado.baixar(baixa("105.50", "3.50", "2", "0", PIX), AGORA);
        assertThat(atrasado.situacao()).isEqualTo(QUITADO);
        assertThat(atrasado.totalPago()).isEqualTo(Dinheiro.de("105.50"));

        Titulo adiantado = titulo("100");
        // pagou 95 com 5 de desconto
        adiantado.baixar(baixa("95", "0", "0", "5", DINHEIRO), AGORA);
        assertThat(adiantado.situacao()).isEqualTo(QUITADO);
    }

    @Test
    void naoBaixaAcimaDoQueDeve() {
        Titulo titulo = titulo("100");
        titulo.baixar(baixa("70", "0", "0", "0", PIX), AGORA);

        assertThatThrownBy(() -> titulo.baixar(baixa("30.01", "0", "0", "0", PIX), AGORA))
                .isInstanceOf(BaixaAcimaDoSaldoException.class)
                .hasMessage("O pagamento abate 30.01, mas o título só deve 30.00");
        assertThatThrownBy(() -> titulo.baixar(baixa("25", "0", "0", "5.01", PIX), AGORA))
                .isInstanceOf(BaixaAcimaDoSaldoException.class);
        assertThat(titulo.baixas()).hasSize(1);
    }

    @Test
    void baixaPrecisaMovimentarDinheiroEAcrescimosNaoPassamDoPago() {
        assertThatThrownBy(() -> baixa("10", "0", "0", "0", FormaDePagamento.A_PRAZO))
                .isInstanceOf(FormaSemMovimentoNaHoraException.class);
        assertThatThrownBy(() -> baixa("10", "6", "4", "0", PIX)).isInstanceOf(ValorInvalidoException.class);
        assertThatThrownBy(() -> baixa("10", "-1", "0", "0", PIX)).isInstanceOf(ValorInvalidoException.class);
        assertThatThrownBy(() -> baixa("0", "0", "0", "0", PIX)).isInstanceOf(ValorInvalidoException.class);
    }

    @Test
    void quitadoNaoRecebeMaisPagamentos() {
        Titulo titulo = titulo("50");
        titulo.baixar(baixa("50", "0", "0", "0", DINHEIRO), AGORA);

        assertThatThrownBy(() -> titulo.baixar(baixa("1", "0", "0", "0", DINHEIRO), AGORA))
                .isInstanceOf(TituloNaoAbertoException.class)
                .hasMessage("O título está quitado e não aceita esta operação");
    }

    @Test
    void estornoDaBaixaDevolveOSaldoUmaVezSo() {
        Titulo titulo = titulo("100");
        titulo.baixar(baixa("100", "0", "0", "0", PIX), AGORA);

        Baixa estornada = titulo.estornarBaixa(1, "PIX devolvido", AGORA);

        assertThat(estornada.estornada()).isTrue();
        assertThat(estornada.motivoDoEstorno()).isEqualTo("PIX devolvido");
        assertThat(titulo.situacao()).isEqualTo(ABERTO);
        assertThat(titulo.saldoDevedor()).isEqualTo(Dinheiro.de("100"));
        assertThatThrownBy(() -> titulo.estornarBaixa(1, "De novo", AGORA)).isInstanceOf(BaixaJaEstornadaException.class);
        assertThatThrownBy(() -> titulo.estornarBaixa(1, " ", AGORA)).isInstanceOf(CampoObrigatorioException.class);
        assertThatThrownBy(() -> titulo.estornarBaixa(9, "x", AGORA)).isInstanceOf(ValorInvalidoException.class);
    }

    @Test
    void cancelaEAlteraSoSemPagamentos() {
        Titulo titulo = titulo("100");
        titulo.baixar(baixa("10", "0", "0", "0", PIX), AGORA);

        assertThatThrownBy(() -> titulo.cancelar("Erro", AGORA)).isInstanceOf(TituloComBaixaException.class);
        assertThatThrownBy(() -> titulo.alterar("Outra", null, null, Dinheiro.de("90"), VENCIMENTO))
                .isInstanceOf(TituloComBaixaException.class);

        titulo.estornarBaixa(1, "Lançado errado", AGORA);
        titulo.alterar("Venda 15 (corrigida)", null, "NF 9", Dinheiro.de("90"), VENCIMENTO.plusDays(5));
        assertThat(titulo.valor()).isEqualTo(Dinheiro.de("90"));

        assertThatThrownBy(() -> titulo.cancelar(" ", AGORA)).isInstanceOf(CampoObrigatorioException.class);
        titulo.cancelar("Venda desfeita", AGORA);
        assertThat(titulo.situacao()).isEqualTo(CANCELADO);
        assertThat(titulo.saldoDevedor()).isEqualTo(Dinheiro.ZERO);
        assertThatThrownBy(() -> titulo.cancelar("De novo", AGORA)).isInstanceOf(TituloNaoAbertoException.class);
    }

    @Test
    void vencidoSoEmAbertoEDepoisDoVencimento() {
        Titulo titulo = titulo("100");

        assertThat(titulo.vencido(VENCIMENTO)).isFalse();
        assertThat(titulo.vencido(VENCIMENTO.plusDays(1))).isTrue();
        assertThat(titulo.diasEmAtraso(VENCIMENTO.plusDays(4))).isEqualTo(4);

        titulo.baixar(baixa("100", "0", "0", "0", PIX), AGORA);
        assertThat(titulo.vencido(VENCIMENTO.plusDays(1))).isFalse();
    }

    @Test
    void contaAReceberExigeCliente() {
        assertThatThrownBy(() -> new DadosDoTitulo(NaturezaDoTitulo.A_RECEBER, null, "Venda", null, null, null, null,
                null)).isInstanceOf(CampoObrigatorioException.class);
        DadosDoTitulo energia = new DadosDoTitulo(NaturezaDoTitulo.A_PAGAR, null, "Energia", null, null,
                FormaDePagamento.BOLETO, null, null);
        assertThat(energia.origem()).isEqualTo(OrigemDoTitulo.MANUAL);
    }

    private static Titulo titulo(String valor) {
        return Titulo.parcelas(1L, CREDIARIO, Parcelamento.aVista(Dinheiro.de(valor), VENCIMENTO)).get(0);
    }

    private static PedidoDeBaixa baixa(String pago, String juros, String multa, String desconto, FormaDePagamento forma) {
        return new PedidoDeBaixa(1L, forma, Dinheiro.de(pago), Dinheiro.de(juros), Dinheiro.de(multa),
                Dinheiro.de(desconto), 7L);
    }
}
