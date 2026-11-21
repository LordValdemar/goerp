package br.com.goerp.cadastros.produto.aplicacao;

import br.com.goerp.cadastros.produto.dominio.UnidadeDeMedida;
import br.com.goerp.cadastros.produto.dominio.UnidadeDeMedidaRepositorio;
import br.com.goerp.compartilhado.aplicacao.CasoDeUso;

/** Altera descrição e se aceita fração. A sigla não muda (vai na nota fiscal). */
@CasoDeUso
public class AlterarUnidadeDeMedida {

    private final UnidadeDeMedidaRepositorio unidades;
    private final BuscarUnidadeDeMedida buscar;

    public AlterarUnidadeDeMedida(UnidadeDeMedidaRepositorio unidades, BuscarUnidadeDeMedida buscar) {
        this.unidades = unidades;
        this.buscar = buscar;
    }

    public UnidadeDeMedida executar(Long id, String descricao, boolean fracionavel) {
        UnidadeDeMedida unidade = buscar.executar(id);
        unidade.alterar(descricao, fracionavel);
        return unidades.salvar(unidade);
    }
}
