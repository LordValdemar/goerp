package br.com.goerp.financeiro.aplicacao;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import br.com.goerp.compartilhado.aplicacao.FusoHorario;
import br.com.goerp.compartilhado.dominio.Dinheiro;
import br.com.goerp.financeiro.dominio.Baixa;
import br.com.goerp.financeiro.dominio.DadosDoTitulo;
import br.com.goerp.financeiro.dominio.FormaDePagamento;
import br.com.goerp.financeiro.dominio.Lancamento;
import br.com.goerp.financeiro.dominio.LancamentoRepositorio;
import br.com.goerp.financeiro.dominio.NaturezaDoTitulo;
import br.com.goerp.financeiro.dominio.OrigemDoLancamento;
import br.com.goerp.financeiro.dominio.OrigemDoTitulo;
import br.com.goerp.financeiro.dominio.Parcela;
import br.com.goerp.financeiro.dominio.PedidoDeLancamento;
import br.com.goerp.financeiro.dominio.SituacaoDoTitulo;
import br.com.goerp.financeiro.dominio.TipoDeLancamento;
import br.com.goerp.financeiro.dominio.Titulo;
import br.com.goerp.financeiro.dominio.TituloRepositorio;

class BaixarTituloTest {

    private static final Instant AGORA = Instant.parse("2026-11-21T12:00:00Z");
    private static final Clock RELOGIO = Clock.fixed(AGORA, ZoneOffset.UTC);
    private static final Long CONTA = 5L;
    private static final Long BAIXA_GRAVADA = 70L;

    private final TituloRepositorio titulos = mock(TituloRepositorio.class);
    private final LancamentoRepositorio lancamentos = mock(LancamentoRepositorio.class);
    private final LancadorFinanceiro lancador = mock(LancadorFinanceiro.class);
    private final BuscarContaFinanceira buscarConta = mock(BuscarContaFinanceira.class);
    private final TitulosDaEmpresa titulosDaEmpresa = new TitulosDaEmpresa(titulos, new FusoHorario(ZoneOffset.UTC),
            RELOGIO);

    @Test
    void pagamentoGeraLancamentoDeSaidaComAFormaACategoriaEOPagamentoComoOrigem() {
        when(titulos.travar(1L)).thenReturn(java.util.Optional.of(titulo(List.of())));
        when(titulos.salvar(any())).thenAnswer(chamada -> comIdNasBaixas(chamada.getArgument(0)));

        var resultado = new BaixarTitulo(titulos, titulosDaEmpresa, buscarConta, lancador, () -> 7L, RELOGIO).executar(
                NaturezaDoTitulo.A_PAGAR, 1L, CONTA, FormaDePagamento.PIX, Dinheiro.de("40"), null, null, null);

        ArgumentCaptor<PedidoDeLancamento> pedido = ArgumentCaptor.forClass(PedidoDeLancamento.class);
        verify(lancador).lancar(eq(CONTA), pedido.capture());
        assertThat(pedido.getValue().tipo()).isEqualTo(TipoDeLancamento.SAIDA);
        assertThat(pedido.getValue().forma()).isEqualTo(FormaDePagamento.PIX);
        assertThat(pedido.getValue().valor()).isEqualTo(Dinheiro.de("40"));
        assertThat(pedido.getValue().categoriaId()).isEqualTo(3L);
        assertThat(pedido.getValue().origem()).isEqualTo(OrigemDoLancamento.BAIXA_DE_TITULO);
        assertThat(pedido.getValue().documentoDeOrigem()).isEqualTo(BAIXA_GRAVADA);
        assertThat(pedido.getValue().descricao()).isEqualTo("Pagamento: Energia (parcela 2/3)");
        assertThat(resultado.titulo().situacao()).isEqualTo(SituacaoDoTitulo.PARCIALMENTE_PAGO);
    }

    @Test
    void estornoDoPagamentoEstornaOLancamentoQueEleGerou() {
        Baixa paga = new Baixa(BAIXA_GRAVADA, 1, CONTA, FormaDePagamento.PIX, Dinheiro.de("40"), Dinheiro.ZERO,
                Dinheiro.ZERO, Dinheiro.ZERO, 7L, AGORA, false, null, null);
        when(titulos.travar(1L)).thenReturn(java.util.Optional.of(titulo(List.of(paga))));
        when(titulos.salvar(any())).thenAnswer(chamada -> chamada.getArgument(0));
        Lancamento doPagamento = new Lancamento(90L, 1L, CONTA, TipoDeLancamento.SAIDA, Dinheiro.de("40"),
                Dinheiro.de("60"), FormaDePagamento.PIX, 3L, "Pagamento", OrigemDoLancamento.BAIXA_DE_TITULO,
                BAIXA_GRAVADA, null, 7L, AGORA);
        when(lancamentos.doDocumento(OrigemDoLancamento.BAIXA_DE_TITULO, BAIXA_GRAVADA)).thenReturn(List.of(doPagamento));

        var resultado = new EstornarBaixa(titulos, titulosDaEmpresa, lancamentos, lancador, () -> 7L, RELOGIO)
                .executar(NaturezaDoTitulo.A_PAGAR, 1L, 1, "PIX devolvido");

        ArgumentCaptor<PedidoDeLancamento> pedido = ArgumentCaptor.forClass(PedidoDeLancamento.class);
        verify(lancador).lancar(eq(CONTA), pedido.capture());
        assertThat(pedido.getValue().tipo()).isEqualTo(TipoDeLancamento.ESTORNO_DE_SAIDA);
        assertThat(pedido.getValue().estornado()).isEqualTo(90L);
        assertThat(pedido.getValue().descricao()).isEqualTo("PIX devolvido");
        assertThat(resultado.titulo().situacao()).isEqualTo(SituacaoDoTitulo.ABERTO);
    }

    private static Titulo titulo(List<Baixa> baixas) {
        var dados = new DadosDoTitulo(NaturezaDoTitulo.A_PAGAR, null, "Energia", 3L, null, FormaDePagamento.BOLETO,
                OrigemDoTitulo.MANUAL, null);
        var parcela = new Parcela(2, 3, Dinheiro.de("100"), LocalDate.of(2026, 12, 10));
        SituacaoDoTitulo situacao = baixas.isEmpty() ? SituacaoDoTitulo.ABERTO : SituacaoDoTitulo.PARCIALMENTE_PAGO;
        return Titulo.reconstituir(1L, 1L, dados, parcela, situacao, baixas, null, null, 0L);
    }

    /** Simula o banco: o pagamento novo ganha id ao ser gravado. */
    private static Titulo comIdNasBaixas(Titulo titulo) {
        List<Baixa> gravadas = titulo.baixas().stream().map(b -> b.id() != null ? b
                : new Baixa(BAIXA_GRAVADA, b.numero(), b.contaId(), b.forma(), b.valorPago(), b.juros(), b.multa(),
                        b.desconto(), b.usuarioId(), b.momento(), b.estornada(), b.motivoDoEstorno(), b.estornadaEm()))
                .toList();
        return Titulo.reconstituir(titulo.id(), titulo.empresaId(), titulo.dados(), titulo.parcelaAtual(),
                titulo.situacao(), gravadas, null, null, 1L);
    }
}
