package br.com.goerp.compras.aplicacao;

import java.time.Clock;

import br.com.goerp.compartilhado.aplicacao.CasoDeUso;
import br.com.goerp.compartilhado.dominio.excecao.RecursoNaoEncontradoException;
import br.com.goerp.compras.dominio.PedidoDeCompra;
import br.com.goerp.compras.dominio.PedidoDeCompraRepositorio;
import br.com.goerp.compras.dominio.Recebimento;
import br.com.goerp.compras.dominio.RecebimentoRepositorio;
import br.com.goerp.estoque.aplicacao.EstornarMovimentacoesDoDocumento;
import br.com.goerp.estoque.dominio.OrigemDaMovimentacao;
import br.com.goerp.financeiro.aplicacao.CancelarTitulosDoDocumento;
import br.com.goerp.financeiro.dominio.OrigemDoTitulo;

/**
 * Desfaz um recebimento lançado errado: o pedido volta a esperar a mercadoria, as contas a pagar
 * da nota são canceladas e as entradas no estoque são estornadas. Se a mercadoria já saiu
 * (vendida) ou alguma conta já foi paga, recusa e nada muda.
 * O custo do cadastro não volta: ele é só uma referência para preço.
 */
@CasoDeUso
public class EstornarRecebimento {

    private final RecebimentoRepositorio recebimentos;
    private final PedidoDeCompraRepositorio pedidos;
    private final ComprasDaEmpresa compras;
    private final EstornarMovimentacoesDoDocumento estornarEstoque;
    private final CancelarTitulosDoDocumento cancelarTitulos;
    private final Clock relogio;

    public EstornarRecebimento(RecebimentoRepositorio recebimentos, PedidoDeCompraRepositorio pedidos,
            ComprasDaEmpresa compras, EstornarMovimentacoesDoDocumento estornarEstoque,
            CancelarTitulosDoDocumento cancelarTitulos, Clock relogio) {
        this.recebimentos = recebimentos;
        this.pedidos = pedidos;
        this.compras = compras;
        this.estornarEstoque = estornarEstoque;
        this.cancelarTitulos = cancelarTitulos;
        this.relogio = relogio;
    }

    public Recebimento executar(Long recebimentoId, String motivo) {
        Recebimento recebimento = recebimentos.buscarPorId(recebimentoId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Recebimento", recebimentoId));
        recebimento.estornar(motivo, relogio.instant());
        PedidoDeCompra pedido = compras.buscar(recebimento.pedidoId());
        pedido.estornarRecebimento(recebimento.itens());
        pedidos.salvar(pedido);
        Recebimento estornado = recebimentos.salvar(recebimento);
        cancelarTitulos.executar(OrigemDoTitulo.RECEBIMENTO_DE_COMPRA, recebimento.id(),
                "Recebimento da NF " + recebimento.notaFiscal() + " estornado: " + recebimento.motivoDoEstorno());
        estornarEstoque.executar(OrigemDaMovimentacao.COMPRA, recebimento.id(),
                "Estorno do recebimento da NF " + recebimento.notaFiscal() + ": " + recebimento.motivoDoEstorno());
        return estornado;
    }
}
