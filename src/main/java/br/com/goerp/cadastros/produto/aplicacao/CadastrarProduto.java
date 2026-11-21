package br.com.goerp.cadastros.produto.aplicacao;

import br.com.goerp.cadastros.produto.dominio.Categoria;
import br.com.goerp.cadastros.produto.dominio.CodigoDeBarrasEmUsoException;
import br.com.goerp.cadastros.produto.dominio.DadosDoProduto;
import br.com.goerp.cadastros.produto.dominio.Produto;
import br.com.goerp.cadastros.produto.dominio.ProdutoJaCadastradoException;
import br.com.goerp.cadastros.produto.dominio.ProdutoRepositorio;
import br.com.goerp.cadastros.produto.dominio.UnidadeDeMedida;
import br.com.goerp.compartilhado.aplicacao.CasoDeUso;
import br.com.goerp.compartilhado.aplicacao.EmpresaAtual;

@CasoDeUso
public class CadastrarProduto {

    private final ProdutoRepositorio produtos;
    private final BuscarUnidadeDeMedida buscarUnidade;
    private final BuscarCategoria buscarCategoria;
    private final EmpresaAtual empresaAtual;

    public CadastrarProduto(ProdutoRepositorio produtos, BuscarUnidadeDeMedida buscarUnidade,
            BuscarCategoria buscarCategoria, EmpresaAtual empresaAtual) {
        this.produtos = produtos;
        this.buscarUnidade = buscarUnidade;
        this.buscarCategoria = buscarCategoria;
        this.empresaAtual = empresaAtual;
    }

    public ProdutoDetalhado executar(String codigo, DadosDoProduto dados) {
        String codigoNormalizado = Produto.normalizarCodigo(codigo);
        if (produtos.existeComCodigo(codigoNormalizado)) {
            throw new ProdutoJaCadastradoException(codigoNormalizado);
        }
        if (dados.codigoDeBarras() != null && produtos.codigoDeBarrasEmUso(dados.codigoDeBarras(), null)) {
            throw new CodigoDeBarrasEmUsoException(dados.codigoDeBarras().valor());
        }
        UnidadeDeMedida unidade = buscarUnidade.executar(dados.unidadeId());
        Categoria categoria = dados.categoriaId() == null ? null : buscarCategoria.executar(dados.categoriaId());

        Produto produto = produtos.salvar(Produto.novo(empresaAtual.id(), codigoNormalizado, dados, unidade, categoria));
        return new ProdutoDetalhado(produto, unidade, categoria);
    }
}
