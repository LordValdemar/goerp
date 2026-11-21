package br.com.goerp.financeiro;

import static br.com.goerp.suporte.Autenticacao.comoUsuarioDaEmpresa;
import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import br.com.goerp.compartilhado.dominio.Dinheiro;
import br.com.goerp.empresa.dominio.Empresa;
import br.com.goerp.financeiro.aplicacao.BaixarTitulo;
import br.com.goerp.financeiro.aplicacao.CadastrarContaFinanceira;
import br.com.goerp.financeiro.aplicacao.LancarTitulo;
import br.com.goerp.financeiro.aplicacao.RegistrarLancamento;
import br.com.goerp.financeiro.dominio.BaixaAcimaDoSaldoException;
import br.com.goerp.financeiro.dominio.DadosDoTitulo;
import br.com.goerp.financeiro.dominio.FormaDePagamento;
import br.com.goerp.financeiro.dominio.NaturezaDoTitulo;
import br.com.goerp.financeiro.dominio.Parcelamento;
import br.com.goerp.financeiro.dominio.TipoDeConta;
import br.com.goerp.financeiro.dominio.TipoDeLancamento.Sentido;
import br.com.goerp.suporte.Cenario;
import br.com.goerp.suporte.TesteDeIntegracao;

/** Duas pessoas pagando a mesma conta ao mesmo tempo: só um pagamento cabe no que ela deve. */
class ConcorrenciaDePagamentosTest extends TesteDeIntegracao {

    @Autowired
    Cenario cenario;

    @Autowired
    CadastrarContaFinanceira cadastrarConta;

    @Autowired
    RegistrarLancamento registrarLancamento;

    @Autowired
    LancarTitulo lancarTitulo;

    @Autowired
    BaixarTitulo baixarTitulo;

    @Test
    void doisPagamentosSimultaneosDe60NumaContaDe100SoUmEntra() throws Exception {
        Empresa empresa = cenario.empresa("11222333000181", "Empresa A LTDA");
        Supplier<Long> banco = () -> cadastrarConta.executar(TipoDeConta.BANCO, "Banco", false).id();
        Long conta = comoUsuarioDaEmpresa(empresa.id(), banco);
        comoUsuarioDaEmpresa(empresa.id(), () -> registrarLancamento.executar(conta, Sentido.ENTRADA, Dinheiro.de("500"),
                FormaDePagamento.TRANSFERENCIA, null, "Saldo inicial"));
        Long titulo = comoUsuarioDaEmpresa(empresa.id(), () -> lancarTitulo.executar(
                new DadosDoTitulo(NaturezaDoTitulo.A_PAGAR, null, "Aluguel", null, null, null, null, null),
                Parcelamento.aVista(Dinheiro.de("100"), LocalDate.now())).get(0).titulo().id());

        ExecutorService threads = Executors.newFixedThreadPool(2);
        CountDownLatch largada = new CountDownLatch(1);
        List<Future<Boolean>> futuros = new ArrayList<>();
        try {
            for (int i = 0; i < 2; i++) {
                futuros.add(threads.submit(() -> {
                    largada.await();
                    try {
                        comoUsuarioDaEmpresa(empresa.id(), () -> baixarTitulo.executar(NaturezaDoTitulo.A_PAGAR, titulo,
                                conta, FormaDePagamento.PIX, Dinheiro.de("60"), null, null, null));
                        return true;
                    } catch (BaixaAcimaDoSaldoException aMais) {
                        return false;
                    }
                }));
            }
            largada.countDown();
            List<Boolean> resultados = new ArrayList<>();
            for (Future<Boolean> futuro : futuros) {
                resultados.add(futuro.get(30, TimeUnit.SECONDS));
            }

            assertThat(resultados).containsExactlyInAnyOrder(true, false);
        } finally {
            threads.shutdownNow();
        }
        assertThat(jdbc.queryForObject("SELECT saldo_devedor FROM titulo", BigDecimal.class)).isEqualByComparingTo("40");
        assertThat(jdbc.queryForObject("SELECT saldo FROM conta_financeira", BigDecimal.class))
                .isEqualByComparingTo("440");
        assertThat(jdbc.queryForObject("SELECT count(*) FROM baixa_titulo", Integer.class)).isEqualTo(1);
    }
}
