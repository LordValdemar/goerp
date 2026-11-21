package br.com.goerp.financeiro;

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
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import br.com.goerp.compartilhado.dominio.Dinheiro;
import br.com.goerp.empresa.dominio.Empresa;
import br.com.goerp.financeiro.aplicacao.CadastrarContaFinanceira;
import br.com.goerp.financeiro.aplicacao.RegistrarLancamento;
import br.com.goerp.financeiro.aplicacao.TransferirEntreContas;
import br.com.goerp.financeiro.dominio.FormaDePagamento;
import br.com.goerp.financeiro.dominio.SaldoInsuficienteException;
import br.com.goerp.financeiro.dominio.TipoDeConta;
import br.com.goerp.financeiro.dominio.TipoDeLancamento.Sentido;
import br.com.goerp.suporte.Cenario;
import br.com.goerp.suporte.TesteDeIntegracao;

/** Vários lançamentos na mesma conta ao mesmo tempo: a trava da conta impede gastar o que não existe. */
class ConcorrenciaFinanceiraTest extends TesteDeIntegracao {

    private static final int OPERACOES_SIMULTANEAS = 10;

    @Autowired
    Cenario cenario;

    @Autowired
    CadastrarContaFinanceira cadastrarConta;

    @Autowired
    RegistrarLancamento registrarLancamento;

    @Autowired
    TransferirEntreContas transferir;

    Empresa empresa;
    Long caixa;
    Long banco;

    @BeforeEach
    void preparar() {
        empresa = cenario.empresa("11222333000181", "Empresa A LTDA");
        caixa = naEmpresa(() -> cadastrarConta.executar(TipoDeConta.CAIXA, "Caixa", false).id());
        banco = naEmpresa(() -> cadastrarConta.executar(TipoDeConta.BANCO, "Banco", false).id());
    }

    @Test
    void dezSaidasDe10DisputandoSaldoDe50PassamExatamenteCinco() throws Exception {
        entrada(caixa, "50");

        List<Boolean> resultados = emParalelo(() -> {
            try {
                naEmpresa(() -> registrarLancamento.executar(caixa, Sentido.SAIDA, Dinheiro.de("10"),
                        FormaDePagamento.DINHEIRO, null, "Pagamento"));
                return true;
            } catch (SaldoInsuficienteException semSaldo) {
                return false;
            }
        });

        assertThat(resultados).filteredOn(pagou -> pagou).hasSize(5);
        assertThat(saldo(caixa)).isEqualByComparingTo("0");
        assertThat(jdbc.queryForObject("SELECT min(saldo_resultante) FROM lancamento_financeiro", BigDecimal.class))
                .isEqualByComparingTo("0");
    }

    @Test
    void transferenciasEmSentidosOpostosNaoSeTravamMutuamente() throws Exception {
        entrada(caixa, "100");
        entrada(banco, "100");
        AtomicInteger vez = new AtomicInteger();

        List<Boolean> resultados = emParalelo(() -> {
            boolean doCaixa = vez.getAndIncrement() % 2 == 0;
            naEmpresa(() -> transferir.executar(doCaixa ? caixa : banco, doCaixa ? banco : caixa, Dinheiro.de("1"), null,
                    "Ida e volta"));
            return true;
        });

        assertThat(resultados).containsOnly(true).hasSize(OPERACOES_SIMULTANEAS);
        assertThat(saldo(caixa)).isEqualByComparingTo("100");
        assertThat(saldo(banco)).isEqualByComparingTo("100");
    }

    private void entrada(Long conta, String valor) {
        naEmpresa(() -> registrarLancamento.executar(conta, Sentido.ENTRADA, Dinheiro.de(valor),
                FormaDePagamento.DINHEIRO, null, "Saldo inicial"));
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

    private <T> T naEmpresa(Supplier<T> acao) {
        return comoUsuarioDaEmpresa(empresa.id(), acao);
    }

    private BigDecimal saldo(Long conta) {
        return jdbc.queryForObject("SELECT saldo FROM conta_financeira WHERE id = ?", BigDecimal.class, conta);
    }
}
