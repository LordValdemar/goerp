package br.com.goerp.estoque.aplicacao;

import br.com.goerp.cadastros.produto.aplicacao.BuscarProduto;
import br.com.goerp.compartilhado.aplicacao.CasoDeUso;
import br.com.goerp.compartilhado.dominio.Pagina;
import br.com.goerp.compartilhado.dominio.Paginacao;
import br.com.goerp.estoque.dominio.Movimentacao;
import br.com.goerp.estoque.dominio.MovimentacaoRepositorio;

/** Extrato (kardex) do produto: movimentações com o saldo após cada uma, das mais recentes às mais antigas. */
@CasoDeUso
public class ConsultarExtrato {

    private final BuscarProduto buscarProduto;
    private final MovimentacaoRepositorio movimentacoes;

    public ConsultarExtrato(BuscarProduto buscarProduto, MovimentacaoRepositorio movimentacoes) {
        this.buscarProduto = buscarProduto;
        this.movimentacoes = movimentacoes;
    }

    public Pagina<Movimentacao> executar(Long produtoId, Paginacao paginacao) {
        buscarProduto.executar(produtoId);
        return movimentacoes.extrato(produtoId, paginacao);
    }
}
