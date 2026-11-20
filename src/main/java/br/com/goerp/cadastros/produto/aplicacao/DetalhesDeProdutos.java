package br.com.goerp.cadastros.produto.aplicacao;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import br.com.goerp.cadastros.produto.dominio.Categoria;
import br.com.goerp.cadastros.produto.dominio.CategoriaRepositorio;
import br.com.goerp.cadastros.produto.dominio.Produto;
import br.com.goerp.cadastros.produto.dominio.UnidadeDeMedida;
import br.com.goerp.cadastros.produto.dominio.UnidadeDeMedidaRepositorio;
import br.com.goerp.compartilhado.aplicacao.ServicoDeAplicacao;
import br.com.goerp.compartilhado.dominio.Pagina;

/**
 * Junta unidade e categoria aos produtos. Numa página, carrega as unidades e categorias da
 * empresa uma vez só (são poucas), em vez de uma consulta por produto.
 */
@ServicoDeAplicacao
public class DetalhesDeProdutos {

    private final UnidadeDeMedidaRepositorio unidades;
    private final CategoriaRepositorio categorias;

    public DetalhesDeProdutos(UnidadeDeMedidaRepositorio unidades, CategoriaRepositorio categorias) {
        this.unidades = unidades;
        this.categorias = categorias;
    }

    public ProdutoDetalhado de(Produto produto) {
        var dados = produto.dados();
        return new ProdutoDetalhado(produto, unidades.buscarPorId(dados.unidadeId()).orElseThrow(),
                dados.categoriaId() == null ? null : categorias.buscarPorId(dados.categoriaId()).orElseThrow());
    }

    public Pagina<ProdutoDetalhado> de(Pagina<Produto> pagina) {
        Map<Long, UnidadeDeMedida> unidadesPorId = unidades.listar(null).stream()
                .collect(Collectors.toMap(UnidadeDeMedida::id, Function.identity()));
        Map<Long, Categoria> categoriasPorId = categorias.listar(null).stream()
                .collect(Collectors.toMap(Categoria::id, Function.identity()));
        return pagina.mapear(produto -> new ProdutoDetalhado(produto,
                unidadesPorId.get(produto.dados().unidadeId()),
                categoriasPorId.get(produto.dados().categoriaId())));
    }
}
