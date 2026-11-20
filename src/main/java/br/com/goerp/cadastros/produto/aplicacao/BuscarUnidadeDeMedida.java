package br.com.goerp.cadastros.produto.aplicacao;

import br.com.goerp.cadastros.produto.dominio.UnidadeDeMedida;
import br.com.goerp.cadastros.produto.dominio.UnidadeDeMedidaRepositorio;
import br.com.goerp.compartilhado.aplicacao.CasoDeUso;
import br.com.goerp.compartilhado.dominio.excecao.RecursoNaoEncontradoException;

@CasoDeUso
public class BuscarUnidadeDeMedida {

    private final UnidadeDeMedidaRepositorio unidades;

    public BuscarUnidadeDeMedida(UnidadeDeMedidaRepositorio unidades) {
        this.unidades = unidades;
    }

    public UnidadeDeMedida executar(Long id) {
        return unidades.buscarPorId(id).orElseThrow(() -> new RecursoNaoEncontradoException("Unidade de medida", id));
    }
}
