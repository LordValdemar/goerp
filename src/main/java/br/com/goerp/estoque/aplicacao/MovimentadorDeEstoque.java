package br.com.goerp.estoque.aplicacao;

import java.time.Clock;
import java.util.Optional;

import br.com.goerp.cadastros.produto.aplicacao.BuscarProduto;
import br.com.goerp.cadastros.produto.aplicacao.ProdutoDetalhado;
import br.com.goerp.compartilhado.aplicacao.EmpresaAtual;
import br.com.goerp.compartilhado.aplicacao.ServicoDeAplicacao;
import br.com.goerp.compartilhado.dominio.Quantidade;
import br.com.goerp.estoque.dominio.ConfiguracaoDeEstoqueRepositorio;
import br.com.goerp.estoque.dominio.Movimentacao;
import br.com.goerp.estoque.dominio.MovimentacaoRepositorio;
import br.com.goerp.estoque.dominio.OrigemDaMovimentacao;
import br.com.goerp.estoque.dominio.PedidoDeMovimentacao;
import br.com.goerp.estoque.dominio.RegrasDeEstoque;
import br.com.goerp.estoque.dominio.SaldoDeEstoque;
import br.com.goerp.estoque.dominio.SaldoDeEstoqueRepositorio;
import br.com.goerp.estoque.dominio.TipoDeMovimentacao;

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
        RegrasDeEstoque regras = regrasDo(produtoId);
        SaldoDeEstoque saldo = saldos.travarParaMovimentar(empresaAtual.id(), produtoId);
        return aplicar(saldo, pedido, regras);
    }

    /**
     * Ajusta o saldo para chegar exatamente na quantidade informada (usado pelo inventário).
     * A diferença é calculada com o saldo já travado, então nenhuma venda simultânea a distorce.
     *
     * @return o ajuste lançado, ou vazio se o saldo já era essa quantidade
     */
    public Optional<Movimentacao> ajustarPara(Long produtoId, Quantidade alvo, String motivo,
            OrigemDaMovimentacao origem, Long documentoDeOrigem, Long usuarioId) {
        RegrasDeEstoque regras = regrasDo(produtoId);
        SaldoDeEstoque saldo = saldos.travarParaMovimentar(empresaAtual.id(), produtoId);
        Quantidade diferenca = alvo.subtrair(saldo.quantidade());
        if (diferenca.compareTo(Quantidade.ZERO) == 0) {
            return Optional.empty();
        }
        boolean falta = diferenca.negativa();
        var pedido = new PedidoDeMovimentacao(
                falta ? TipoDeMovimentacao.AJUSTE_DE_SAIDA : TipoDeMovimentacao.AJUSTE_DE_ENTRADA,
                falta ? Quantidade.ZERO.subtrair(diferenca) : diferenca,
                null, origem, documentoDeOrigem, motivo, null, usuarioId);
        return Optional.of(aplicar(saldo, pedido, regras));
    }

    /** As regras do produto (da empresa atual: produto de outra empresa dá 404). */
    public RegrasDeEstoque regrasDo(Long produtoId) {
        ProdutoDetalhado produto = buscarProduto.executar(produtoId);
        return new RegrasDeEstoque(produto.produto().dados().controlaEstoque(), produto.unidade().fracionavel(),
                configuracao.permiteEstoqueNegativo());
    }

    private Movimentacao aplicar(SaldoDeEstoque saldo, PedidoDeMovimentacao pedido, RegrasDeEstoque regras) {
        Movimentacao movimentacao = saldo.movimentar(pedido, regras, relogio.instant());
        saldos.salvar(saldo);
        return movimentacoes.registrar(movimentacao);
    }
}
