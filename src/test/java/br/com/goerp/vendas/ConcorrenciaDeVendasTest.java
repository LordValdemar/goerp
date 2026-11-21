package br.com.goerp.vendas;

import static br.com.goerp.suporte.Autenticacao.comoUsuario;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import br.com.goerp.cadastros.produto.aplicacao.CadastrarProduto;
import br.com.goerp.cadastros.produto.dominio.CustoUnitario;
import br.com.goerp.cadastros.produto.dominio.DadosDoProduto;
import br.com.goerp.cadastros.produto.dominio.DadosFiscais;
import br.com.goerp.compartilhado.dominio.Dinheiro;
import br.com.goerp.compartilhado.dominio.Ncm;
import br.com.goerp.compartilhado.dominio.Quantidade;
import br.com.goerp.empresa.dominio.Empresa;
import br.com.goerp.estoque.aplicacao.RegistrarEntrada;
import br.com.goerp.estoque.dominio.EstoqueInsuficienteException;
import br.com.goerp.estoque.dominio.OrigemDaMovimentacao;
import br.com.goerp.suporte.Cenario;
import br.com.goerp.suporte.TesteDeIntegracao;
import br.com.goerp.vendas.aplicacao.AbrirCaixa;
import br.com.goerp.vendas.aplicacao.RegistrarVenda;
import br.com.goerp.vendas.aplicacao.SolicitacaoDeVenda;
import br.com.goerp.vendas.aplicacao.SolicitacaoDeVenda.ItemSolicitado;
import br.com.goerp.vendas.dominio.FormaDePagamento;
import br.com.goerp.vendas.dominio.Pagamento;

/** Vários caixas vendendo ao mesmo tempo. */
class ConcorrenciaDeVendasTest extends TesteDeIntegracao {

    @Autowired
    Cenario cenario;

    @Autowired
    CadastrarProduto cadastrarProduto;

    @Autowired
    RegistrarEntrada registrarEntrada;

    @Autowired
    AbrirCaixa abrirCaixa;

    @Autowired
    RegistrarVenda registrarVenda;

    Empresa empresa;
    Long ana;
    Long bia;
    Long produto;

    @BeforeEach
    void preparar() {
        empresa = cenario.empresa("11222333000181", "Mercado A LTDA");
        ana = cenario.usuario("ana@a.com").id();
        bia = cenario.usuario("bia@a.com").id();
        Long unidade = jdbc.queryForObject("SELECT id FROM unidade_medida WHERE empresa_id = ? AND sigla = 'UN'",
                Long.class, empresa.id());
        produto = como(ana, () -> cadastrarProduto.executar("REF", new DadosDoProduto("Refrigerante", null, null,
                unidade, Dinheiro.de("10"), null, new DadosFiscais(Ncm.de("22021000"), null, null), true, null))
                .produto().id());
        como(ana, () -> abrirCaixa.executar(Dinheiro.ZERO));
        como(bia, () -> abrirCaixa.executar(Dinheiro.ZERO));
    }

    @Test
    void doisCaixasDisputandoOUltimoItemSoUmVende() throws Exception {
        entrada("1");

        List<Boolean> resultados = emParalelo(List.of(() -> tentarVender(ana), () -> tentarVender(bia)));

        assertThat(resultados).containsExactlyInAnyOrder(true, false);
        assertThat(jdbc.queryForObject("SELECT count(*) FROM venda", Integer.class)).isEqualTo(1);
        assertThat(jdbc.queryForObject("SELECT quantidade FROM saldo_estoque", java.math.BigDecimal.class))
                .isEqualByComparingTo("0");
    }

    @Test
    void vendasSimultaneasRecebemNumerosDistintosESemBuracos() throws Exception {
        entrada("100");
        List<Callable<Boolean>> vendas = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            Long operador = i % 2 == 0 ? ana : bia;
            vendas.add(() -> tentarVender(operador));
        }

        assertThat(emParalelo(vendas)).containsOnly(true);
        assertThat(jdbc.queryForList("SELECT numero FROM venda ORDER BY numero", Long.class))
                .containsExactly(1L, 2L, 3L, 4L, 5L, 6L, 7L, 8L, 9L, 10L);
    }

    private boolean tentarVender(Long operador) {
        try {
            como(operador, () -> registrarVenda.executar(new SolicitacaoDeVenda(
                    List.of(new ItemSolicitado(produto, null, Quantidade.de("1"), null)), null,
                    List.of(new Pagamento(FormaDePagamento.DINHEIRO, Dinheiro.de("10"))), null, null)));
            return true;
        } catch (EstoqueInsuficienteException semEstoque) {
            return false;
        }
    }

    private void entrada(String quantidade) {
        como(ana, () -> registrarEntrada.executar(produto, Quantidade.de(quantidade), CustoUnitario.de("5"),
                OrigemDaMovimentacao.MANUAL, null, null));
    }

    private <T> T como(Long usuario, Supplier<T> acao) {
        return comoUsuario(usuario, empresa.id(), acao);
    }

    private static List<Boolean> emParalelo(List<Callable<Boolean>> operacoes) throws Exception {
        ExecutorService threads = Executors.newFixedThreadPool(operacoes.size());
        CountDownLatch largada = new CountDownLatch(1);
        try {
            List<Future<Boolean>> futuros = new ArrayList<>();
            for (Callable<Boolean> operacao : operacoes) {
                futuros.add(threads.submit(() -> {
                    largada.await();
                    return operacao.call();
                }));
            }
            largada.countDown();
            List<Boolean> resultados = new ArrayList<>();
            for (Future<Boolean> futuro : futuros) {
                resultados.add(futuro.get(30, TimeUnit.SECONDS));
            }
            return resultados;
        } finally {
            threads.shutdownNow();
        }
    }
}
