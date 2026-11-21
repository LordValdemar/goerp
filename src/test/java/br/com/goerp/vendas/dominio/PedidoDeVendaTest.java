package br.com.goerp.vendas.dominio;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;

import br.com.goerp.compartilhado.dominio.Dinheiro;
import br.com.goerp.compartilhado.dominio.Quantidade;
import br.com.goerp.compartilhado.dominio.excecao.ValorInvalidoException;

class PedidoDeVendaTest {

    private static final LocalDate HOJE = LocalDate.of(2026, 11, 21);
    private static final Instant AGORA = Instant.parse("2026-11-21T15:00:00Z");
    private static final PoliticaDeDesconto SEM_DESCONTO = new PoliticaDeDesconto(BigDecimal.TEN, false);
    private static final ItemDeVenda ARROZ = new ItemDeVenda(1L, "ARZ", "Arroz 5kg", Quantidade.de("10"),
            Dinheiro.de("24.90"), null);

    @Test
    void nasceComoOrcamentoComTotal() {
        PedidoDeVenda pedido = orcar(HOJE.plusDays(7));

        assertThat(pedido.situacao()).isEqualTo(SituacaoDoPedido.ORCAMENTO);
        assertThat(pedido.total()).isEqualTo(Dinheiro.de("249"));
    }

    @Test
    void fluxoCompletoOrcamentoConfirmadoFaturado() {
        PedidoDeVenda pedido = orcar(HOJE.plusDays(7));

        pedido.confirmar(HOJE, AGORA);
        assertThat(pedido.situacao()).isEqualTo(SituacaoDoPedido.CONFIRMADO);
        pedido.faturar(AGORA);
        assertThat(pedido.situacao()).isEqualTo(SituacaoDoPedido.FATURADO);
    }

    @Test
    void soOrcamentoPodeSerAlterado() {
        PedidoDeVenda pedido = orcar(HOJE.plusDays(7));
        pedido.confirmar(HOJE, AGORA);

        assertThatThrownBy(() -> pedido.alterar(conteudo(HOJE.plusDays(7)), SEM_DESCONTO, HOJE))
                .isInstanceOf(TransicaoDePedidoInvalidaException.class)
                .hasMessage("Pedido confirmado não pode ser alterado");
    }

    @Test
    void naoFaturaSemConfirmarNemCancelaFaturado() {
        PedidoDeVenda pedido = orcar(HOJE.plusDays(7));

        assertThatThrownBy(() -> pedido.faturar(AGORA)).isInstanceOf(TransicaoDePedidoInvalidaException.class);
        pedido.confirmar(HOJE, AGORA);
        pedido.faturar(AGORA);
        assertThatThrownBy(() -> pedido.cancelar("Desistiu", AGORA))
                .isInstanceOf(TransicaoDePedidoInvalidaException.class);
    }

    @Test
    void orcamentoVencidoNaoPodeSerConfirmado() {
        PedidoDeVenda pedido = orcar(HOJE);

        assertThatThrownBy(() -> pedido.confirmar(HOJE.plusDays(1), AGORA)).isInstanceOf(OrcamentoVencidoException.class);
        pedido.confirmar(HOJE, AGORA);
    }

    @Test
    void validadeNoPassadoEhRecusada() {
        assertThatThrownBy(() -> orcar(HOJE.minusDays(1))).isInstanceOf(ValorInvalidoException.class);
    }

    @Test
    void cancelaOrcamentoOuConfirmadoComMotivo() {
        PedidoDeVenda pedido = orcar(HOJE.plusDays(7));
        pedido.confirmar(HOJE, AGORA);

        pedido.cancelar("Cliente desistiu", AGORA);

        assertThat(pedido.situacao()).isEqualTo(SituacaoDoPedido.CANCELADO);
        assertThatThrownBy(() -> pedido.confirmar(HOJE, AGORA)).isInstanceOf(TransicaoDePedidoInvalidaException.class);
    }

    @Test
    void descontoSegueAMesmaPoliticaDoPdv() {
        var conteudoComDesconto = new PedidoDeVenda.Conteudo(List.of(ARROZ), Dinheiro.de("20"), HOJE, null);

        assertThatThrownBy(() -> PedidoDeVenda.orcar(1L, 1L, 2L, 3L, conteudoComDesconto, SEM_DESCONTO, HOJE, AGORA))
                .isInstanceOf(DescontoNaoPermitidoException.class);
        assertThat(PedidoDeVenda.orcar(1L, 1L, 2L, 3L, conteudoComDesconto,
                new PoliticaDeDesconto(BigDecimal.TEN, true), HOJE, AGORA).total()).isEqualTo(Dinheiro.de("229"));
    }

    private static PedidoDeVenda orcar(LocalDate validade) {
        return PedidoDeVenda.orcar(1L, 1L, 2L, 3L, conteudo(validade), SEM_DESCONTO, HOJE, AGORA);
    }

    private static PedidoDeVenda.Conteudo conteudo(LocalDate validade) {
        return new PedidoDeVenda.Conteudo(List.of(ARROZ), null, validade, null);
    }
}
