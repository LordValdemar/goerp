package br.com.goerp.financeiro.web;

import br.com.goerp.financeiro.dominio.CategoriaFinanceira;
import br.com.goerp.financeiro.dominio.TipoDeCategoria;

public record CategoriaFinanceiraResponse(Long id, TipoDeCategoria tipo, String nome, boolean ativa) {

    static CategoriaFinanceiraResponse de(CategoriaFinanceira categoria) {
        return new CategoriaFinanceiraResponse(categoria.id(), categoria.tipo(), categoria.nome(), categoria.ativa());
    }
}
