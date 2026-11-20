package br.com.goerp.estoque.dominio;

import static br.com.goerp.estoque.dominio.TipoDeMovimentacao.AJUSTE_DE_ENTRADA;
import static br.com.goerp.estoque.dominio.TipoDeMovimentacao.AJUSTE_DE_SAIDA;
import static br.com.goerp.estoque.dominio.TipoDeMovimentacao.ENTRADA;
import static br.com.goerp.estoque.dominio.TipoDeMovimentacao.SAIDA;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;

import org.junit.jupiter.api.Test;

import br.com.goerp.cadastros.produto.dominio.CustoUnitario;
import br.com.goerp.compartilhado.dominio.Quantidade;

class SaldoDeEstoqueTest {

    private static final Instant AGORA = Instant.parse("2026-11-20T12:00:00Z");
    private static final RegrasDeEstoque UNIDADE_INTEIRA = new RegrasDeEstoque(true, false, false);
    private static final RegrasDeEstoque PERMITE_NEGATIVO = new RegrasDeEstoque(true, false, true);
    private static final RegrasDeEstoque QUILO = new RegrasDeEstoque(true, true, false);

    private final SaldoDeEstoque saldo = SaldoDeEstoque.zerado(1L, 10L);

    // ---------- custo médio ----------

    @Test
    void primeiraEntradaDefineOCustoMedio() {
        Movimentacao entrada = entrar("10", "5.00");

        assertThat(saldo.quantidade()).isEqualTo(Quantidade.de("10"));
        assertThat(saldo.custoMedio()).isEqualTo(CustoUnitario.de("5"));
        assertThat(entrada.saldoResultante()).isEqualTo(Quantidade.de("10"));
    }

    @Test
    void entradasRecalculamOCustoMedioPonderado() {
        entrar("10", "5.00");
        entrar("10", "7.00");
        assertThat(saldo.custoMedio()).isEqualTo(CustoUnitario.de("6"));

        entrar("5", "12.00");
        // (20 x 6 + 5 x 12) / 25 = 180 / 25 = 7,20
        assertThat(saldo.custoMedio()).isEqualTo(CustoUnitario.de("7.2"));
    }

    @Test
    void custoMedioComDizimaFicaCom4Casas() {
        entrar("3", "10.00");
        entrar("3", "10.01");
        entrar("3", "10.01");

        // (30 + 30,03 + 30,03) / 9 = 10,00666...
        assertThat(saldo.custoMedio()).isEqualTo(CustoUnitario.de("10.0067"));
    }

    @Test
    void entradaSemCustoUsaOCustoMedioAtual() {
        entrar("10", "5.00");

        Movimentacao semCusto = saldo.movimentar(pedido(ENTRADA, "10", null, null), UNIDADE_INTEIRA, AGORA);

        assertThat(semCusto.custoUnitario()).isEqualTo(CustoUnitario.de("5"));
        assertThat(saldo.custoMedio()).isEqualTo(CustoUnitario.de("5"));
    }

    @Test
    void saidaUsaOCustoMedioENaoOAltera() {
        entrar("10", "5.00");
        entrar("10", "7.00");

        Movimentacao saida = sair("4");

        assertThat(saida.custoUnitario()).isEqualTo(CustoUnitario.de("6"));
        assertThat(saida.saldoResultante()).isEqualTo(Quantidade.de("16"));
        assertThat(saldo.custoMedio()).isEqualTo(CustoUnitario.de("6"));
    }

    // ---------- saldo insuficiente ----------

    @Test
    void saidaMaiorQueOSaldoEhRecusada() {
        entrar("3", "5.00");

        assertThatThrownBy(() -> sair("4"))
                .isInstanceOf(EstoqueInsuficienteException.class)
                .hasMessageContaining("disponível 3");
        assertThat(saldo.quantidade()).isEqualTo(Quantidade.de("3"));
    }

    @Test
    void empresaQuePermiteNegativoDeixaOSaldoFicarNegativo() {
        entrar("3", "5.00");

        Movimentacao saida = saldo.movimentar(pedido(SAIDA, "5", null, null), PERMITE_NEGATIVO, AGORA);

        assertThat(saida.saldoResultante()).isEqualTo(Quantidade.de("-2"));
    }

    @Test
    void entradaDepoisDeSaldoNegativoUsaOCustoDaEntrada() {
        entrar("3", "5.00");
        saldo.movimentar(pedido(SAIDA, "5", null, null), PERMITE_NEGATIVO, AGORA);

        entrar("10", "8.00");

        assertThat(saldo.quantidade()).isEqualTo(Quantidade.de("8"));
        assertThat(saldo.custoMedio()).isEqualTo(CustoUnitario.de("8"));
    }

    // ---------- regras do produto ----------

    @Test
    void unidadeInteiraNaoAceitaFracaoMasQuiloAceita() {
        assertThatThrownBy(() -> saldo.movimentar(pedido(ENTRADA, "1.5", "5", null), UNIDADE_INTEIRA, AGORA))
                .isInstanceOf(QuantidadeFracionadaException.class);

        assertThat(saldo.movimentar(pedido(ENTRADA, "1.25", "5", null), QUILO, AGORA).saldoResultante())
                .isEqualTo(Quantidade.de("1.25"));
    }

    @Test
    void produtoSemControleDeEstoqueNaoMovimenta() {
        assertThatThrownBy(() -> saldo.movimentar(pedido(ENTRADA, "1", "5", null), new RegrasDeEstoque(false, true, true),
                AGORA)).isInstanceOf(ProdutoSemControleDeEstoqueException.class);
    }

    @Test
    void quantidadeZeroOuNegativaEhInvalida() {
        assertThatThrownBy(() -> pedido(ENTRADA, "0", "5", null)).isInstanceOf(QuantidadeInvalidaException.class);
        assertThatThrownBy(() -> pedido(SAIDA, "-1", null, null)).isInstanceOf(QuantidadeInvalidaException.class);
    }

    @Test
    void ajusteExigeMotivo() {
        assertThatThrownBy(() -> pedido(AJUSTE_DE_SAIDA, "1", null, " ")).isInstanceOf(MotivoObrigatorioException.class);

        entrar("10", "5.00");
        Movimentacao perda = saldo.movimentar(pedido(AJUSTE_DE_SAIDA, "2", null, "Quebra no transporte"),
                UNIDADE_INTEIRA, AGORA);
        Movimentacao sobra = saldo.movimentar(pedido(AJUSTE_DE_ENTRADA, "1", null, "Contagem"), UNIDADE_INTEIRA, AGORA);

        assertThat(perda.efeitoNoSaldo()).isEqualTo(Quantidade.de("-2"));
        assertThat(sobra.efeitoNoSaldo()).isEqualTo(Quantidade.de("1"));
        assertThat(saldo.quantidade()).isEqualTo(Quantidade.de("9"));
        assertThat(saldo.custoMedio()).isEqualTo(CustoUnitario.de("5"));
    }

    // ---------- estornos ----------

    @Test
    void estornoDeEntradaDevolveSaldoECustoMedioAnteriores() {
        entrar("10", "5.00");
        Movimentacao errada = comId(entrar("10", "7.00"));

        saldo.movimentar(errada.pedidoDeEstorno("Nota lançada em dobro", 1L), UNIDADE_INTEIRA, AGORA);

        assertThat(saldo.quantidade()).isEqualTo(Quantidade.de("10"));
        assertThat(saldo.custoMedio()).isEqualTo(CustoUnitario.de("5"));
    }

    @Test
    void estornoDeSaidaDevolveAoEstoqueComOCustoDaSaida() {
        entrar("10", "6.00");
        Movimentacao saida = comId(sair("4"));
        entrar("6", "10.00");

        Movimentacao estorno = saldo.movimentar(saida.pedidoDeEstorno("Venda cancelada", 1L), UNIDADE_INTEIRA, AGORA);

        assertThat(estorno.tipo()).isEqualTo(TipoDeMovimentacao.ESTORNO_DE_SAIDA);
        assertThat(estorno.custoUnitario()).isEqualTo(CustoUnitario.de("6"));
        assertThat(estorno.estornada()).isEqualTo(saida.id());
        assertThat(saldo.quantidade()).isEqualTo(Quantidade.de("16"));
    }

    @Test
    void estornoDeEntradaSemSaldoSuficienteEhRecusado() {
        Movimentacao entrada = comId(entrar("10", "5.00"));
        sair("8");

        assertThatThrownBy(() -> saldo.movimentar(entrada.pedidoDeEstorno("Erro", 1L), UNIDADE_INTEIRA, AGORA))
                .isInstanceOf(EstoqueInsuficienteException.class);
    }

    @Test
    void estornoNaoPodeSerEstornadoEExigeMotivo() {
        Movimentacao entrada = comId(entrar("10", "5.00"));
        Movimentacao estorno = comId(saldo.movimentar(entrada.pedidoDeEstorno("Erro", 1L), UNIDADE_INTEIRA, AGORA));

        assertThatThrownBy(() -> estorno.pedidoDeEstorno("De novo", 1L)).isInstanceOf(EstornoDeEstornoException.class);
        assertThatThrownBy(() -> entrada.pedidoDeEstorno(null, 1L)).isInstanceOf(MotivoObrigatorioException.class);
    }

    private Movimentacao entrar(String quantidade, String custo) {
        return saldo.movimentar(pedido(ENTRADA, quantidade, custo, null), UNIDADE_INTEIRA, AGORA);
    }

    private Movimentacao sair(String quantidade) {
        return saldo.movimentar(pedido(SAIDA, quantidade, null, null), UNIDADE_INTEIRA, AGORA);
    }

    private static PedidoDeMovimentacao pedido(TipoDeMovimentacao tipo, String quantidade, String custo, String motivo) {
        return new PedidoDeMovimentacao(tipo, Quantidade.de(quantidade), custo == null ? null : CustoUnitario.de(custo),
                OrigemDaMovimentacao.MANUAL, null, motivo, null, 1L);
    }

    private static long proximoId = 100;

    /** Simula a gravação, que dá um id à movimentação. */
    private static Movimentacao comId(Movimentacao m) {
        return new Movimentacao(proximoId++, m.empresaId(), m.produtoId(), m.tipo(), m.quantidade(), m.custoUnitario(),
                m.saldoResultante(), m.custoMedioResultante(), m.origem(), m.documentoDeOrigem(), m.motivo(),
                m.estornada(), m.usuarioId(), m.momento());
    }
}
