package br.com.goerp.estoque.aplicacao;

import java.time.Clock;

import br.com.goerp.cadastros.produto.aplicacao.BuscarProduto;
import br.com.goerp.cadastros.produto.aplicacao.ProdutoDetalhado;
import br.com.goerp.compartilhado.aplicacao.EmpresaAtual;
import br.com.goerp.compartilhado.aplicacao.ServicoDeAplicacao;
import br.com.goerp.estoque.dominio.ConfiguracaoDeEstoqueRepositorio;
import br.com.goerp.estoque.dominio.Movimentacao;
import br.com.goerp.estoque.dominio.MovimentacaoRepositorio;
import br.com.goerp.estoque.dominio.PedidoDeMovimentacao;
import br.com.goerp.estoque.dominio.RegrasDeEstoque;
import br.com.goerp.estoque.dominio.SaldoDeEstoque;
import br.com.goerp.estoque.dominio.SaldoDeEstoqueRepositorio;

/**
 * Único caminho para mexer no estoque: busca o produto (da empresa atual), trava o saldo,
 * aplica as regras, grava o novo saldo e a movimentação. Roda dentro da transação de quem chama
 * (entrada manual, venda, compra...): se a operação maior falhar, a movimentação também é desfeita.
 */
@ServicoDeAplicacao
public class MovimentadorDeEstoque {

    private final BuscarProduto buscarProduto;
    private final SaldoDeEstoqueRepositorio saldos;
    private final MovimentacaoRepositorio movimentacoes;
    private final ConfiguracaoDeEstoqueRepositorio configuracao;
    private final EmpresaAtual empresaAtual;
    private final Clock relogio;

    public MovimentadorDeEstoque(BuscarProduto buscarProduto, SaldoDeEstoqueRepositorio saldos,
            MovimentacaoRepositorio movimentacoes, ConfiguracaoDeEstoqueRepositorio configuracao,
            EmpresaAtual empresaAtual, Clock relogio) {
        this.buscarProduto = buscarProduto;
        this.saldos = saldos;
        this.movimentacoes = movimentacoes;
        this.configuracao = configuracao;
        this.empresaAtual = empresaAtual;
        this.relogio = relogio;
    }

    public Movimentacao movimentar(Long produtoId, PedidoDeMovimentacao pedido) {
        ProdutoDetalhado produto = buscarProduto.executar(produtoId);
        var regras = new RegrasDeEstoque(produto.produto().dados().controlaEstoque(), produto.unidade().fracionavel(),
                configuracao.permiteEstoqueNegativo());

        SaldoDeEstoque saldo = saldos.travarParaMovimentar(empresaAtual.id(), produtoId);
        Movimentacao movimentacao = saldo.movimentar(pedido, regras, relogio.instant());
        saldos.salvar(saldo);
        return movimentacoes.registrar(movimentacao);
    }
}
