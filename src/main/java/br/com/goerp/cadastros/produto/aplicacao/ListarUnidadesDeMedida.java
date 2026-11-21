package br.com.goerp.cadastros.produto.aplicacao;

import java.util.List;

import br.com.goerp.cadastros.produto.dominio.UnidadeDeMedida;
import br.com.goerp.cadastros.produto.dominio.UnidadeDeMedidaRepositorio;
import br.com.goerp.compartilhado.aplicacao.CasoDeUso;

@CasoDeUso
public class ListarUnidadesDeMedida {

    private final UnidadeDeMedidaRepositorio unidades;

    public ListarUnidadesDeMedida(UnidadeDeMedidaRepositorio unidades) {
        this.unidades = unidades;
    }

    public List<UnidadeDeMedida> executar(Boolean ativa) {
        return unidades.listar(ativa);
    }
}
