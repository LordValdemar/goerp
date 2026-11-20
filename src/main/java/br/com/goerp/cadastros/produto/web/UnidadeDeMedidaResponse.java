package br.com.goerp.cadastros.produto.web;

import br.com.goerp.cadastros.produto.dominio.UnidadeDeMedida;

public record UnidadeDeMedidaResponse(Long id, String sigla, String descricao, boolean fracionavel, boolean ativa) {

    static UnidadeDeMedidaResponse de(UnidadeDeMedida unidade) {
        return new UnidadeDeMedidaResponse(unidade.id(), unidade.sigla(), unidade.descricao(), unidade.fracionavel(),
                unidade.ativa());
    }
}
