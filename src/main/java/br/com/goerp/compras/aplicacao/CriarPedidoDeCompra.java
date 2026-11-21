package br.com.goerp.compras.aplicacao;

import java.time.Clock;

import br.com.goerp.cadastros.fornecedor.aplicacao.BuscarFornecedor;
import br.com.goerp.compartilhado.aplicacao.CasoDeUso;
import br.com.goerp.compartilhado.aplicacao.EmpresaAtual;
import br.com.goerp.compartilhado.aplicacao.UsuarioAtual;
import br.com.goerp.compartilhado.dominio.excecao.ValorInvalidoException;
import br.com.goerp.compras.dominio.PedidoDeCompra;
import br.com.goerp.compras.dominio.PedidoDeCompraRepositorio;

/** Cria o pedido em rascunho, para um fornecedor ativo da empresa. */
@CasoDeUso
public class CriarPedidoDeCompra {

    private final PedidoDeCompraRepositorio pedidos;
    private final ComprasDaEmpresa compras;
    private final BuscarFornecedor buscarFornecedor;
    private final EmpresaAtual empresaAtual;
    private final UsuarioAtual usuarioAtual;
    private final Clock relogio;

    public CriarPedidoDeCompra(PedidoDeCompraRepositorio pedidos, ComprasDaEmpresa compras,
            BuscarFornecedor buscarFornecedor, EmpresaAtual empresaAtual, UsuarioAtual usuarioAtual, Clock relogio) {
        this.pedidos = pedidos;
        this.compras = compras;
        this.buscarFornecedor = buscarFornecedor;
        this.empresaAtual = empresaAtual;
        this.usuarioAtual = usuarioAtual;
        this.relogio = relogio;
    }

    public PedidoDeCompra executar(Long fornecedorId, DadosDoPedidoDeCompra dados) {
        if (!buscarFornecedor.executar(fornecedorId).ativo()) {
            throw new ValorInvalidoException("Fornecedor inativo não pode receber pedido de compra");
        }
        PedidoDeCompra.Conteudo conteudo = compras.conteudo(dados);
        return pedidos.salvar(PedidoDeCompra.rascunho(empresaAtual.id(), pedidos.proximoNumero(empresaAtual.id()),
                fornecedorId, usuarioAtual.id(), conteudo, relogio.instant()));
    }
}
