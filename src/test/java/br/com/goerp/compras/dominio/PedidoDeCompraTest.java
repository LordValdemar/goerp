package br.com.goerp.compras.dominio;

import static br.com.goerp.compras.dominio.SituacaoDoPedidoDeCompra.ENCERRADO;
import static br.com.goerp.compras.dominio.SituacaoDoPedidoDeCompra.ENVIADO;
import static br.com.goerp.compras.dominio.SituacaoDoPedidoDeCompra.PARCIALMENTE_RECEBIDO;
import static br.com.goerp.compras.dominio.SituacaoDoPedidoDeCompra.RECEBIDO;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;

import br.com.goerp.cadastros.produto.dominio.CustoUnitario;
import br.com.goerp.compartilhado.dominio.Dinheiro;
import br.com.goerp.compartilhado.dominio.Quantidade;
import br.com.goerp.compartilhado.dominio.excecao.CampoObrigatorioException;

class PedidoDeCompraTest {

    private static final Instant AGORA = Instant.parse("2026-11-21T12:00:00Z");
    private static final LocalDate PREVISAO = LocalDate.of(2026, 11, 25);
    private static final ItemDeCompra ARROZ = ItemDeCompra.novo(1L, "ARZ", "Arroz 5kg", Quantidade.de("100"),
            CustoUnitario.de("18.3333"));
    private static final ItemDeCompra FEIJAO = ItemDeCompra.novo(2L, "FEJ", "Feijão 1kg", Quantidade.de("50"),
            CustoUnitario.de("7.50"));

    @Test
    void totalComCustoDeQuatroCasasArredondadoACentavos() {
        // 100 x 18,3333 = 1833,33 ; 50 x 7,50 = 375,00
        assertThat(enviado().total()).isEqualTo(Dinheiro.de("2208.33"));
    }

    @Test
    void recebimentoParcialEDepoisCompleto() {
        PedidoDeCompra pedido = enviado();

        pedido.receber(List.of(recebido(1L, "60")));
        assertThat(pedido.situacao()).isEqualTo(PARCIALMENTE_RECEBIDO);
        assertThat(pedido.item(1L).pendente()).isEqualTo(Quantidade.de("40"));

        pedido.receber(List.of(recebido(1L, "40"), recebido(2L, "50")));
        assertThat(pedido.situacao()).isEqualTo(RECEBIDO);
    }

    @Test
    void naoRecebeMaisQueOPendenteNemAplicaPelaMetade() {
        PedidoDeCompra pedido = enviado();

        assertThatThrownBy(() -> pedido.receber(List.of(recebido(2L, "10"), recebido(1L, "101"))))
                .isInstanceOf(QuantidadeAcimaDoPendenteException.class)
                .hasMessageContaining("ARZ tem só 100");

        assertThat(pedido.item(2L).recebida()).isEqualTo(Quantidade.ZERO);
        assertThat(pedido.situacao()).isEqualTo(ENVIADO);
    }

    @Test
    void produtoForaDoPedidoEhRecusado() {
        assertThatThrownBy(() -> enviado().receber(List.of(recebido(99L, "1"))))
                .isInstanceOf(ProdutoForaDoPedidoException.class);
    }

    @Test
    void estornoDeRecebimentoVoltaAEsperar() {
        PedidoDeCompra pedido = enviado();
        pedido.receber(List.of(recebido(1L, "100"), recebido(2L, "50")));

        pedido.estornarRecebimento(List.of(recebido(2L, "50")));
        assertThat(pedido.situacao()).isEqualTo(PARCIALMENTE_RECEBIDO);

        pedido.estornarRecebimento(List.of(recebido(1L, "100")));
        assertThat(pedido.situacao()).isEqualTo(ENVIADO);
    }

    @Test
    void rascunhoAlteraEEnviadoNao() {
        PedidoDeCompra pedido = rascunho();
        pedido.alterar(new PedidoDeCompra.Conteudo(List.of(ARROZ), PREVISAO, null));
        pedido.enviar(AGORA);

        assertThatThrownBy(() -> pedido.alterar(new PedidoDeCompra.Conteudo(List.of(FEIJAO), PREVISAO, null)))
                .isInstanceOf(TransicaoDeCompraInvalidaException.class)
                .hasMessage("Pedido de compra enviado não pode ser alterado");
    }

    @Test
    void rascunhoNaoRecebe() {
        assertThatThrownBy(() -> rascunho().receber(List.of(recebido(1L, "1"))))
                .isInstanceOf(TransicaoDeCompraInvalidaException.class);
    }

    @Test
    void cancelaSoSemRecebimentoEEncerraSaldoSoComParcial() {
        PedidoDeCompra semNada = enviado();
        assertThatThrownBy(() -> semNada.encerrarSaldo("Fornecedor sem estoque", AGORA))
                .isInstanceOf(TransicaoDeCompraInvalidaException.class);
        semNada.cancelar("Comprado de outro fornecedor", AGORA);

        PedidoDeCompra parcial = enviado();
        parcial.receber(List.of(recebido(1L, "10")));
        assertThatThrownBy(() -> parcial.cancelar("Desisti", AGORA)).isInstanceOf(TransicaoDeCompraInvalidaException.class);
        assertThatThrownBy(() -> parcial.encerrarSaldo(" ", AGORA)).isInstanceOf(CampoObrigatorioException.class);
        parcial.encerrarSaldo("Fornecedor sem estoque", AGORA);
        assertThat(parcial.situacao()).isEqualTo(ENCERRADO);
    }

    @Test
    void produtoRepetidoEPedidoVazioSaoRecusados() {
        assertThatThrownBy(() -> PedidoDeCompra.rascunho(1L, 1L, 2L, 3L,
                new PedidoDeCompra.Conteudo(List.of(ARROZ, ARROZ), null, null), AGORA))
                .isInstanceOf(ProdutoRepetidoNoPedidoException.class);
        assertThatThrownBy(() -> PedidoDeCompra.rascunho(1L, 1L, 2L, 3L,
                new PedidoDeCompra.Conteudo(List.of(), null, null), AGORA))
                .isInstanceOf(PedidoDeCompraSemItensException.class);
    }

    @Test
    void atrasadoQuandoAPrevisaoPassouESeguemFaltandoItens() {
        PedidoDeCompra pedido = enviado();

        assertThat(pedido.atrasado(PREVISAO)).isFalse();
        assertThat(pedido.atrasado(PREVISAO.plusDays(1))).isTrue();
        pedido.receber(List.of(recebido(1L, "100"), recebido(2L, "50")));
        assertThat(pedido.atrasado(PREVISAO.plusDays(1))).isFalse();
    }

    @Test
    void recebimentoEstornaUmaVezSo() {
        Recebimento recebimento = Recebimento.registrar(1L, 10L, "NF 1234", List.of(recebido(1L, "10")), 3L, AGORA);

        assertThat(recebimento.valorTotal()).isEqualTo(Dinheiro.de("183.33"));
        recebimento.estornar("Nota lançada no pedido errado", AGORA);
        assertThatThrownBy(() -> recebimento.estornar("De novo", AGORA))
                .isInstanceOf(RecebimentoJaEstornadoException.class);
        assertThatThrownBy(() -> Recebimento.registrar(1L, 10L, " ", List.of(recebido(1L, "1")), 3L, AGORA))
                .isInstanceOf(CampoObrigatorioException.class);
    }

    @Test
    void valorPendenteEhOQueFaltaChegarEZeraQuandoNaoEsperaMais() {
        PedidoDeCompra pedido = enviado();
        pedido.receber(List.of(recebido(1L, "60")));

        // 40 x 18,3333 = 733,332 -> 733,33 ; 50 x 7,50 = 375,00
        assertThat(pedido.valorPendente()).isEqualTo(Dinheiro.de("1108.33"));
        assertThat(rascunho().valorPendente()).isEqualTo(Dinheiro.ZERO);
        pedido.encerrarSaldo("Fornecedor sem estoque", AGORA);
        assertThat(pedido.valorPendente()).isEqualTo(Dinheiro.ZERO);
    }

    private static PedidoDeCompra rascunho() {
        return PedidoDeCompra.rascunho(1L, 1L, 2L, 3L,
                new PedidoDeCompra.Conteudo(List.of(ARROZ, FEIJAO), PREVISAO, null), AGORA);
    }

    private static PedidoDeCompra enviado() {
        PedidoDeCompra pedido = rascunho();
        pedido.enviar(AGORA);
        return pedido;
    }

    private static ItemRecebido recebido(Long produtoId, String quantidade) {
        return new ItemRecebido(produtoId, Quantidade.de(quantidade), CustoUnitario.de("18.3333"));
    }
}
