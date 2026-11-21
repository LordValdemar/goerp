package br.com.goerp.compras;

import static br.com.goerp.suporte.Autenticacao.comoUsuario;
import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import br.com.goerp.cadastros.fornecedor.aplicacao.CadastrarFornecedor;
import br.com.goerp.cadastros.fornecedor.dominio.DadosDoFornecedor;
import br.com.goerp.cadastros.produto.aplicacao.CadastrarProduto;
import br.com.goerp.cadastros.produto.dominio.DadosDoProduto;
import br.com.goerp.cadastros.produto.dominio.DadosFiscais;
import br.com.goerp.compartilhado.dominio.Dinheiro;
import br.com.goerp.compartilhado.dominio.Documento;
import br.com.goerp.compartilhado.dominio.Ncm;
import br.com.goerp.compartilhado.dominio.Quantidade;
import br.com.goerp.compras.aplicacao.AndamentoDoPedidoDeCompra;
import br.com.goerp.compras.aplicacao.CriarPedidoDeCompra;
import br.com.goerp.compras.aplicacao.DadosDoPedidoDeCompra;
import br.com.goerp.compras.aplicacao.ItemInformadoNoRecebimento;
import br.com.goerp.compras.aplicacao.ItemSolicitadoNaCompra;
import br.com.goerp.compras.aplicacao.RegistrarRecebimento;
import br.com.goerp.empresa.dominio.Empresa;
import br.com.goerp.suporte.Cenario;
import br.com.goerp.suporte.TesteDeIntegracao;

/** Duas pessoas recebendo a mesma entrega ao mesmo tempo não dão entrada em dobro. */
class ConcorrenciaDeRecebimentosTest extends TesteDeIntegracao {

    @Autowired
    Cenario cenario;

    @Autowired
    CadastrarProduto cadastrarProduto;

    @Autowired
    CadastrarFornecedor cadastrarFornecedor;

    @Autowired
    CriarPedidoDeCompra criarPedido;

    @Autowired
    AndamentoDoPedidoDeCompra andamento;

    @Autowired
    RegistrarRecebimento registrarRecebimento;

    Empresa empresa;
    Long usuario;
    Long produto;
    Long pedido;

    @BeforeEach
    void preparar() {
        empresa = cenario.empresa("11222333000181", "Mercado A LTDA");
        usuario = cenario.usuario("ana@a.com").id();
        Long un = jdbc.queryForObject("SELECT id FROM unidade_medida WHERE empresa_id = ? AND sigla = 'UN'", Long.class,
                empresa.id());
        produto = como(() -> cadastrarProduto.executar("ARZ", new DadosDoProduto("Arroz", null, null, un,
                Dinheiro.de("30"), null, new DadosFiscais(Ncm.de("10063021"), null, null), true, null)).produto().id());
        Long fornecedor = como(() -> cadastrarFornecedor.executar(Documento.de("11444777000161"),
                new DadosDoFornecedor("Atacado", null, null, null, null, null, null, null)).id());
        pedido = como(() -> criarPedido.executar(fornecedor, new DadosDoPedidoDeCompra(
                List.of(new ItemSolicitadoNaCompra(produto, Quantidade.de("100"), null)), null, null)).id());
        como(() -> andamento.enviar(pedido));
    }

    @Test
    void doisRecebimentosSimultaneosDe60NumPedidoDe100SoUmEntra() throws Exception {
        ExecutorService threads = Executors.newFixedThreadPool(2);
        CountDownLatch largada = new CountDownLatch(1);
        List<Future<Boolean>> futuros = new ArrayList<>();
        for (int i = 0; i < 2; i++) {
            String nota = "NF " + i;
            futuros.add(threads.submit(() -> {
                largada.await();
                try {
                    como(() -> registrarRecebimento.executar(pedido, nota,
                            List.of(new ItemInformadoNoRecebimento(produto, Quantidade.de("60"), null)), null));
                    return true;
                } catch (RuntimeException recusado) {
                    return false;
                }
            }));
        }
        largada.countDown();
        List<Boolean> resultados = new ArrayList<>();
        for (Future<Boolean> futuro : futuros) {
            resultados.add(futuro.get(30, TimeUnit.SECONDS));
        }
        threads.shutdownNow();

        assertThat(resultados).containsExactlyInAnyOrder(true, false);
        assertThat(jdbc.queryForObject("SELECT count(*) FROM recebimento", Integer.class)).isEqualTo(1);
        assertThat(jdbc.queryForObject("SELECT recebida FROM pedido_compra_item", BigDecimal.class))
                .isEqualByComparingTo("60");
        assertThat(jdbc.queryForObject("SELECT quantidade FROM saldo_estoque", BigDecimal.class))
                .isEqualByComparingTo("60");
    }

    private <T> T como(Supplier<T> acao) {
        return comoUsuario(usuario, empresa.id(), acao);
    }
}
