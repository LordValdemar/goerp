package br.com.goerp.cadastros.produto.web;

import java.math.BigDecimal;

import br.com.goerp.cadastros.produto.dominio.CustoUnitario;
import br.com.goerp.cadastros.produto.dominio.DadosDoProduto;
import br.com.goerp.cadastros.produto.dominio.DadosFiscais;
import br.com.goerp.cadastros.produto.dominio.OrigemDaMercadoria;
import br.com.goerp.compartilhado.dominio.Cest;
import br.com.goerp.compartilhado.dominio.Dinheiro;
import br.com.goerp.compartilhado.dominio.Gtin;
import br.com.goerp.compartilhado.dominio.Ncm;
import br.com.goerp.compartilhado.dominio.Quantidade;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * @param codigo só no cadastro (depois não muda)
 * @param versao só na alteração: a versão que foi lida
 */
public record ProdutoRequest(
        String codigo,
        @NotBlank @Size(max = 120) String descricao,
        String codigoDeBarras,
        Long categoriaId,
        @NotNull Long unidadeId,
        @NotNull BigDecimal precoDeVenda,
        BigDecimal precoDeCusto,
        @NotBlank String ncm,
        String cest,
        OrigemDaMercadoria origem,
        boolean controlaEstoque,
        BigDecimal estoqueMinimo,
        Long versao) {

    DadosDoProduto paraDados() {
        return new DadosDoProduto(descricao,
                codigoDeBarras == null || codigoDeBarras.isBlank() ? null : Gtin.de(codigoDeBarras),
                categoriaId, unidadeId, Dinheiro.de(precoDeVenda),
                precoDeCusto == null ? null : new CustoUnitario(precoDeCusto),
                new DadosFiscais(Ncm.de(ncm), cest == null || cest.isBlank() ? null : Cest.de(cest), origem),
                controlaEstoque,
                estoqueMinimo == null ? null : new Quantidade(estoqueMinimo));
    }
}
