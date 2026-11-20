package br.com.goerp.cadastros.produto.dominio;

import java.util.Optional;

import br.com.goerp.compartilhado.dominio.Gtin;
import br.com.goerp.compartilhado.dominio.Pagina;
import br.com.goerp.compartilhado.dominio.Paginacao;

/** Produtos da empresa atual (o filtro por empresa é automático). */
public interface ProdutoRepositorio {

    Produto salvar(Produto produto);

    Optional<Produto> buscarPorId(Long id);

    boolean existeComCodigo(String codigo);

    /** @param idIgnorado nulo no cadastro; o próprio produto na alteração */
    boolean codigoDeBarrasEmUso(Gtin codigoDeBarras, Long idIgnorado);

    Pagina<Produto> pesquisar(FiltroDeProdutos filtro, Paginacao paginacao);

    Optional<Produto> buscarAtivoPorCodigoDeBarras(Gtin codigoDeBarras);
}
