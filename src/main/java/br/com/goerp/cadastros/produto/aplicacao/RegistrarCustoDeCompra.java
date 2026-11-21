package br.com.goerp.cadastros.produto.aplicacao;

import br.com.goerp.cadastros.produto.dominio.CustoUnitario;
import br.com.goerp.cadastros.produto.dominio.Produto;
import br.com.goerp.cadastros.produto.dominio.ProdutoRepositorio;
import br.com.goerp.compartilhado.aplicacao.CasoDeUso;
import br.com.goerp.compartilhado.dominio.excecao.RecursoNaoEncontradoException;

/** Chamado pelo recebimento de compras: o custo do cadastro passa a ser o último custo de compra. */
@CasoDeUso
public class RegistrarCustoDeCompra {

    private final ProdutoRepositorio produtos;

    public RegistrarCustoDeCompra(ProdutoRepositorio produtos) {
        this.produtos = produtos;
    }

    public void executar(Long produtoId, CustoUnitario custo) {
        Produto produto = produtos.buscarPorId(produtoId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Produto", produtoId));
        produto.registrarCustoDeCompra(custo);
        produtos.salvar(produto);
    }
}
