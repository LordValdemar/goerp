package br.com.goerp.vendas.dominio;

import static br.com.goerp.financeiro.dominio.FormaDePagamento.CARTAO_CREDITO;
import static br.com.goerp.financeiro.dominio.FormaDePagamento.DINHEIRO;
import static br.com.goerp.financeiro.dominio.FormaDePagamento.PIX;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import br.com.goerp.compartilhado.dominio.Dinheiro;
import br.com.goerp.compartilhado.dominio.Quantidade;
import br.com.goerp.compartilhado.dominio.excecao.CampoObrigatorioException;
import br.com.goerp.compartilhado.dominio.excecao.ValorInvalidoException;
import br.com.goerp.financeiro.dominio.FormaDePagamento;

class VendaTest {

    private static final Instant AGORA = Instant.parse("2026-11-21T10:00:00Z");
    private static final PoliticaDeDesconto SEM_PERMISSAO = new PoliticaDeDesconto(BigDecimal.TEN, false);
    private static final PoliticaDeDesconto ATE_10_POR_CENTO = new PoliticaDeDesconto(BigDecimal.TEN, true);

    private static final ItemDeVenda REFRIGERANTE = item(1L, "2", "8.99", "0");
    private static final ItemDeVenda QUEIJO = new ItemDeVenda(2L, "QJO", "Queijo", Quantidade.de("0.350"),
            Dinheiro.de("49.90"), null);

    @Test
    void calculaTotaisComArredondamentoPorItem() {
        Venda venda = registrar(List.of(REFRIGERANTE, QUEIJO), "0", SEM_PERMISSAO, dinheiro("40"));

        // 2 x 8,99 = 17,98 ; 0,350 kg x 49,90 = 17,465 -> 17,46 (meio para o par)
        assertThat(venda.bruto()).isEqualTo(Dinheiro.de("35.44"));
        assertThat(venda.total()).isEqualTo(Dinheiro.de("35.44"));
        assertThat(venda.troco()).isEqualTo(Dinheiro.de("4.56"));
        assertThat(venda.situacao()).isEqualTo(SituacaoDaVenda.FINALIZADA);
    }

    @Test
    void trocoSoSaiDoDinheiroERecebidoPorFormaDescontaOTroco() {
        Venda venda = registrar(List.of(REFRIGERANTE), "0", SEM_PERMISSAO,
                new Pagamento(PIX, Dinheiro.de("10")), new Pagamento(DINHEIRO, Dinheiro.de("10")));

        assertThat(venda.troco()).isEqualTo(Dinheiro.de("2.02"));
        assertThat(venda.recebidoPorForma()).containsExactlyInAnyOrderEntriesOf(Map.of(
                PIX, Dinheiro.de("10"), DINHEIRO, Dinheiro.de("7.98")));
    }

    @Test
    void cartaoOuPixAcimaDoTotalSaoRecusados() {
        assertThatThrownBy(() -> registrar(List.of(REFRIGERANTE), "0", SEM_PERMISSAO,
                new Pagamento(CARTAO_CREDITO, Dinheiro.de("20")))).isInstanceOf(PagamentoAcimaDoTotalException.class);
        // o PIX sozinho já passa do total (17,98): não há troco de PIX
        assertThatThrownBy(() -> registrar(List.of(REFRIGERANTE), "0", SEM_PERMISSAO,
                new Pagamento(PIX, Dinheiro.de("18")), new Pagamento(DINHEIRO, Dinheiro.de("2"))))
                .isInstanceOf(PagamentoAcimaDoTotalException.class);
    }

    @Test
    void parteNoPixEOrestoEmDinheiroComTroco() {
        Venda venda = registrar(List.of(REFRIGERANTE), "0", SEM_PERMISSAO,
                new Pagamento(PIX, Dinheiro.de("15")), new Pagamento(DINHEIRO, Dinheiro.de("4")));

        assertThat(venda.troco()).isEqualTo(Dinheiro.de("1.02"));
    }

    @Test
    void pdvSoAceitaFormasConferidasNoCaixa() {
        assertThatThrownBy(() -> new Pagamento(FormaDePagamento.BOLETO, Dinheiro.de("10")))
                .isInstanceOf(FormaNaoAceitaNoPdvException.class)
                .hasMessage("A forma de pagamento BOLETO não é aceita no PDV");
        assertThatThrownBy(() -> new Pagamento(FormaDePagamento.A_PRAZO, Dinheiro.de("10")))
                .isInstanceOf(FormaNaoAceitaNoPdvException.class);
    }

    @Test
    void pagamentoInsuficienteEhRecusado() {
        assertThatThrownBy(() -> registrar(List.of(REFRIGERANTE), "0", SEM_PERMISSAO, dinheiro("17.97")))
                .isInstanceOf(PagamentoInsuficienteException.class);
    }

    @Test
    void vendaSemItensEhRecusada() {
        assertThatThrownBy(() -> registrar(List.of(), "0", SEM_PERMISSAO, dinheiro("1")))
                .isInstanceOf(VendaSemItensException.class);
    }

    @Test
    void descontoExigePermissaoERespeitaOLimite() {
        assertThatThrownBy(() -> registrar(List.of(REFRIGERANTE), "1", SEM_PERMISSAO, dinheiro("20")))
                .isInstanceOf(DescontoNaoPermitidoException.class);

        // limite 10% de 17,98 = 1,798: 1,79 passa, 1,80 não
        assertThat(registrar(List.of(REFRIGERANTE), "1.79", ATE_10_POR_CENTO, dinheiro("16.19")).total())
                .isEqualTo(Dinheiro.de("16.19"));
        assertThatThrownBy(() -> registrar(List.of(REFRIGERANTE), "1.80", ATE_10_POR_CENTO, dinheiro("20")))
                .isInstanceOf(DescontoAcimaDoLimiteException.class)
                .hasMessageContaining("10%");
    }

    @Test
    void descontoNoItemTambemConta() {
        ItemDeVenda comDesconto = item(1L, "2", "8.99", "1.98");

        assertThatThrownBy(() -> registrar(List.of(comDesconto), "0", SEM_PERMISSAO, dinheiro("16")))
                .isInstanceOf(DescontoNaoPermitidoException.class);
        assertThat(registrar(List.of(item(1L, "2", "8.99", "1.79")), "0", ATE_10_POR_CENTO, dinheiro("16.19"))
                .descontoTotal()).isEqualTo(Dinheiro.de("1.79"));
    }

    @Test
    void itemComQuantidadeOuDescontoInvalidos() {
        assertThatThrownBy(() -> item(1L, "0", "8.99", "0")).isInstanceOf(ValorInvalidoException.class);
        assertThatThrownBy(() -> item(1L, "1", "8.99", "9.00")).isInstanceOf(ValorInvalidoException.class);
    }

    @Test
    void cancelaUmaVezSoComMotivo() {
        Venda venda = registrar(List.of(REFRIGERANTE), "0", SEM_PERMISSAO, dinheiro("20"));

        assertThatThrownBy(() -> venda.cancelar(" ", 1L, AGORA)).isInstanceOf(CampoObrigatorioException.class);
        venda.cancelar("Cliente desistiu", 1L, AGORA);

        assertThat(venda.situacao()).isEqualTo(SituacaoDaVenda.CANCELADA);
        assertThatThrownBy(() -> venda.cancelar("De novo", 1L, AGORA)).isInstanceOf(VendaJaCanceladaException.class);
    }

    private static Venda registrar(List<ItemDeVenda> itens, String desconto, PoliticaDeDesconto politica,
            Pagamento... pagamentos) {
        return Venda.registrar(new Venda.DadosDaVenda(1L, 2L, 3L, null, null, itens, Dinheiro.de(desconto),
                List.of(pagamentos)), politica, AGORA);
    }

    private static Pagamento dinheiro(String valor) {
        return new Pagamento(DINHEIRO, Dinheiro.de(valor));
    }

    private static ItemDeVenda item(Long produto, String quantidade, String preco, String desconto) {
        return new ItemDeVenda(produto, "REF", "Refrigerante 2L", Quantidade.de(quantidade), Dinheiro.de(preco),
                Dinheiro.de(desconto));
    }
}
