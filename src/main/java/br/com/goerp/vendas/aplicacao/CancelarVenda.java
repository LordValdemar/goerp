package br.com.goerp.vendas.aplicacao;

import java.time.Clock;

import br.com.goerp.compartilhado.aplicacao.CasoDeUso;
import br.com.goerp.compartilhado.aplicacao.UsuarioAtual;
import br.com.goerp.estoque.aplicacao.EstornarMovimentacoesDoDocumento;
import br.com.goerp.estoque.dominio.OrigemDaMovimentacao;
import br.com.goerp.vendas.dominio.Caixa;
import br.com.goerp.vendas.dominio.CaixaDaVendaFechadoException;
import br.com.goerp.vendas.dominio.CaixaRepositorio;
import br.com.goerp.vendas.dominio.Venda;
import br.com.goerp.vendas.dominio.VendaRepositorio;

/**
 * Cancela a venda enquanto o caixa dela está aberto: a mercadoria volta ao estoque (estorno) e
 * o valor deixa de ser esperado no fechamento. Depois do fechamento, o caminho é a devolução.
 */
@CasoDeUso
public class CancelarVenda {

    private final VendaRepositorio vendas;
    private final CaixaRepositorio caixas;
    private final BuscarVenda buscarVenda;
    private final EstornarMovimentacoesDoDocumento estornarEstoque;
    private final UsuarioAtual usuarioAtual;
    private final Clock relogio;

    public CancelarVenda(VendaRepositorio vendas, CaixaRepositorio caixas, BuscarVenda buscarVenda,
            EstornarMovimentacoesDoDocumento estornarEstoque, UsuarioAtual usuarioAtual, Clock relogio) {
        this.vendas = vendas;
        this.caixas = caixas;
        this.buscarVenda = buscarVenda;
        this.estornarEstoque = estornarEstoque;
        this.usuarioAtual = usuarioAtual;
        this.relogio = relogio;
    }

    public Venda executar(Long vendaId, String motivo) {
        Venda venda = buscarVenda.executar(vendaId);
        Caixa caixa = caixas.buscarPorId(venda.caixaId()).orElseThrow();
        if (!caixa.aberto()) {
            throw new CaixaDaVendaFechadoException();
        }
        venda.cancelar(motivo, usuarioAtual.id(), relogio.instant());
        Venda cancelada = vendas.salvar(venda);
        estornarEstoque.executar(OrigemDaMovimentacao.VENDA, venda.id(),
                "Cancelamento da venda nº " + venda.numero() + ": " + venda.motivoDoCancelamento());
        return cancelada;
    }
}
