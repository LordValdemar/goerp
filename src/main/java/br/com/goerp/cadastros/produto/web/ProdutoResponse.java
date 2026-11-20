package br.com.goerp.cadastros.produto.web;

import java.math.BigDecimal;

import br.com.goerp.cadastros.produto.aplicacao.ProdutoDetalhado;
import br.com.goerp.cadastros.produto.dominio.OrigemDaMercadoria;

public record ProdutoResponse(
        Long id,
        String codigo,
        String descricao,
        String codigoDeBarras,
        Unidade unidade,
        CategoriaResumo categoria,
        BigDecimal precoDeVenda,
        BigDecimal precoDeCusto,
        String ncm,
        String cest,
        OrigemDaMercadoria origem,
        boolean controlaEstoque,
        BigDecimal estoqueMinimo,
        boolean ativo,
        Long versao) {

    public record Unidade(Long id, String sigla, boolean fracionavel) {
    }

    public record CategoriaResumo(Long id, String nome) {
    }

    static ProdutoResponse de(ProdutoDetalhado detalhado) {
        var produto = detalhado.produto();
        var dados = produto.dados();
        var unidade = detalhado.unidade();
        var categoria = detalhado.categoria();
        return new ProdutoResponse(produto.id(), produto.codigo(), dados.descricao(),
                dados.codigoDeBarras() == null ? null : dados.codigoDeBarras().valor(),
                new Unidade(unidade.id(), unidade.sigla(), unidade.fracionavel()),
                categoria == null ? null : new CategoriaResumo(categoria.id(), categoria.nome()),
                dados.precoDeVenda().valor(), dados.precoDeCusto().valor(),
                dados.fiscais().ncm().valor(),
                dados.fiscais().cest() == null ? null : dados.fiscais().cest().valor(),
                dados.fiscais().origem(), dados.controlaEstoque(), dados.estoqueMinimo().valor(),
                produto.ativo(), produto.versao());
    }
}
