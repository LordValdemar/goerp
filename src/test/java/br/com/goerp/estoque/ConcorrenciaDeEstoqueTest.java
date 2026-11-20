package br.com.goerp.estoque;

import static br.com.goerp.suporte.Autenticacao.comoUsuarioDaEmpresa;
import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

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
import br.com.goerp.estoque.aplicacao.RegistrarSaida;
import br.com.goerp.estoque.dominio.EstoqueInsuficienteException;
import br.com.goerp.estoque.dominio.OrigemDaMovimentacao;
import br.com.goerp.suporte.Cenario;
import br.com.goerp.suporte.TesteDeIntegracao;

/**
 * Várias operações no mesmo produto ao mesmo tempo (ex.: vários caixas vendendo o último item).
 * Prova que a trava do saldo impede vender mais do que existe e que o saldo nunca se perde.
 */
class ConcorrenciaDeEstoqueTest extends TesteDeIntegracao {

    private static final int OPERACOES_SIMULTANEAS = 10;

    @Autowired
    Cenario cenario;

    @Autowired
    CadastrarProduto cadastrarProduto;

    @Autowired
    RegistrarEntrada registrarEntrada;

    @Autowired
    RegistrarSaida registrarSaida;

    Empresa empresa;
    Long produto;

    @BeforeEach
    void preparar() {
        empresa = cenario.empresa("11222333000181", "Empresa A LTDA");
        Long unidade = jdbc.queryForObject("SELECT id FROM unidade_medida WHERE empresa_id = ? AND sigla = 'UN'",
                Long.class, empresa.id());
        produto = naEmpresa(() -> cadastrarProduto.executar("REF", new DadosDoProduto("Refrigerante", null, null, unidade,
                Dinheiro.de("8"), null, new DadosFiscais(Ncm.de("22021000"), null, null), true, null))
                .produto().id());
    }

    @Test
    void dezVendasDisputandoCincoUnidadesVendemExatamenteCinco() throws Exception {
        naEmpresa(() -> registrarEntrada.executar(produto, Quantidade.de("5"), CustoUnitario.de("5"),
                OrigemDaMovimentacao.MANUAL, null, null));

        List<Boolean> resultados = emParalelo(() -> {
            try {
                naEmpresa(() -> registrarSaida.executar(produto, Quantidade.de("1"), OrigemDaMovimentacao.VENDA, null,
                        null));
                return true;
            } catch (EstoqueInsuficienteException semEstoque) {
                return false;
            }
        });

        assertThat(resultados).filteredOn(vendeu -> vendeu).hasSize(5);
        assertThat(saldo()).isEqualByComparingTo("0");
        assertThat(jdbc.queryForObject("SELECT count(*) FROM movimentacao_estoque WHERE tipo = 'SAIDA'", Integer.class))
                .isEqualTo(5);
    }

    @Test
    void primeirasEntradasSimultaneasNaoSePerdemNemColidem() throws Exception {
        List<Boolean> resultados = emParalelo(() -> {
            naEmpresa(() -> registrarEntrada.executar(produto, Quantidade.de("1"), CustoUnitario.de("5"),
                    OrigemDaMovimentacao.MANUAL, null, null));
            return true;
        });

        assertThat(resultados).containsOnly(true).hasSize(OPERACOES_SIMULTANEAS);
        assertThat(saldo()).isEqualByComparingTo(String.valueOf(OPERACOES_SIMULTANEAS));
        assertThat(jdbc.queryForObject("SELECT max(saldo_resultante) FROM movimentacao_estoque", BigDecimal.class))
                .isEqualByComparingTo(String.valueOf(OPERACOES_SIMULTANEAS));
    }

    private List<Boolean> emParalelo(Callable<Boolean> operacao) throws Exception {
        ExecutorService threads = Executors.newFixedThreadPool(OPERACOES_SIMULTANEAS);
        CountDownLatch largada = new CountDownLatch(1);
        try {
            List<Future<Boolean>> futuros = new ArrayList<>();
            for (int i = 0; i < OPERACOES_SIMULTANEAS; i++) {
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

    private <T> T naEmpresa(java.util.function.Supplier<T> acao) {
        return comoUsuarioDaEmpresa(empresa.id(), acao);
    }

    private BigDecimal saldo() {
        return jdbc.queryForObject("SELECT quantidade FROM saldo_estoque WHERE produto_id = ?", BigDecimal.class, produto);
    }
}
