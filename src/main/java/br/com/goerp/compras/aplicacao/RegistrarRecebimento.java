package br.com.goerp.compras.aplicacao;

import java.time.Clock;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

import br.com.goerp.cadastros.produto.aplicacao.RegistrarCustoDeCompra;
import br.com.goerp.compartilhado.aplicacao.CasoDeUso;
import br.com.goerp.compartilhado.aplicacao.EmpresaAtual;
import br.com.goerp.compartilhado.aplicacao.UsuarioAtual;
import br.com.goerp.compras.dominio.ItemRecebido;
import br.com.goerp.compras.dominio.PedidoDeCompra;
import br.com.goerp.compras.dominio.PedidoDeCompraRepositorio;
import br.com.goerp.compras.dominio.Recebimento;
import br.com.goerp.compras.dominio.RecebimentoRepositorio;
import br.com.goerp.estoque.aplicacao.RegistrarEntrada;
import br.com.goerp.estoque.dominio.OrigemDaMovimentacao;

/**
 * Chegada de mercadoria de um pedido, numa única transação: atualiza o que falta receber,
 * grava o recebimento, dá entrada no estoque (o custo da nota entra no custo médio) e registra o
 * último custo de compra no cadastro. Se qualquer parte falhar, nada é gravado.
 */
@CasoDeUso
public class RegistrarRecebimento {

    private final PedidoDeCompraRepositorio pedidos;
    private final RecebimentoRepositorio recebimentos;
    private final ComprasDaEmpresa compras;
    private final RegistrarEntrada registrarEntrada;
    private final RegistrarCustoDeCompra registrarCusto;
    private final EmpresaAtual empresaAtual;
    private final UsuarioAtual usuarioAtual;
    private final Clock relogio;

    public RegistrarRecebimento(PedidoDeCompraRepositorio pedidos, RecebimentoRepositorio recebimentos,
            ComprasDaEmpresa compras, RegistrarEntrada registrarEntrada, RegistrarCustoDeCompra registrarCusto,
            EmpresaAtual empresaAtual, UsuarioAtual usuarioAtual, Clock relogio) {
        this.pedidos = pedidos;
        this.recebimentos = recebimentos;
        this.compras = compras;
        this.registrarEntrada = registrarEntrada;
        this.registrarCusto = registrarCusto;
        this.empresaAtual = empresaAtual;
        this.usuarioAtual = usuarioAtual;
        this.relogio = relogio;
    }

    public Recebimento executar(Long pedidoId, String notaFiscal, List<ItemInformadoNoRecebimento> informados) {
        PedidoDeCompra pedido = compras.buscar(pedidoId);
        List<ItemRecebido> itens = informados.stream()
                .map(informado -> new ItemRecebido(informado.produtoId(), informado.quantidade(),
                        Objects.requireNonNullElse(informado.custoUnitario(),
                                pedido.item(informado.produtoId()).custoNegociado())))
                .toList();

        pedido.receber(itens);
        pedidos.salvar(pedido);
        Recebimento recebimento = recebimentos.salvar(Recebimento.registrar(empresaAtual.id(), pedidoId, notaFiscal,
                itens, usuarioAtual.id(), relogio.instant()));
        darEntradaNoEstoque(recebimento, pedido);
        return recebimento;
    }

    /** Em ordem de produto, como nas vendas: travas sempre na mesma sequência. */
    private void darEntradaNoEstoque(Recebimento recebimento, PedidoDeCompra pedido) {
        String observacao = "Recebimento da NF " + recebimento.notaFiscal() + " do pedido de compra nº " + pedido.numero();
        recebimento.itens().stream()
                .sorted(Comparator.comparing(ItemRecebido::produtoId))
                .forEach(item -> {
                    registrarEntrada.executar(item.produtoId(), item.quantidade(), item.custoUnitario(),
                            OrigemDaMovimentacao.COMPRA, recebimento.id(), observacao);
                    registrarCusto.executar(item.produtoId(), item.custoUnitario());
                });
    }
}
