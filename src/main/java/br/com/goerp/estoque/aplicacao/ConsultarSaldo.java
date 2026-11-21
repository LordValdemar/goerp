package br.com.goerp.estoque.aplicacao;

import br.com.goerp.cadastros.produto.aplicacao.BuscarProduto;
import br.com.goerp.compartilhado.aplicacao.CasoDeUso;
import br.com.goerp.compartilhado.aplicacao.EmpresaAtual;
import br.com.goerp.estoque.dominio.SaldoDeEstoque;
import br.com.goerp.estoque.dominio.SaldoDeEstoqueRepositorio;

/** Saldo de um produto da empresa; zero se ainda não teve movimentação. */
@CasoDeUso
public class ConsultarSaldo {

    private final BuscarProduto buscarProduto;
    private final SaldoDeEstoqueRepositorio saldos;
    private final EmpresaAtual empresaAtual;

    public ConsultarSaldo(BuscarProduto buscarProduto, SaldoDeEstoqueRepositorio saldos, EmpresaAtual empresaAtual) {
        this.buscarProduto = buscarProduto;
        this.saldos = saldos;
        this.empresaAtual = empresaAtual;
    }

    public SaldoDeEstoque executar(Long produtoId) {
        buscarProduto.executar(produtoId);
        return saldos.buscar(produtoId).orElseGet(() -> SaldoDeEstoque.zerado(empresaAtual.id(), produtoId));
    }
}
