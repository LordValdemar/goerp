package br.com.goerp.cadastros.produto.aplicacao;

import br.com.goerp.cadastros.produto.dominio.UnidadeDeMedida;
import br.com.goerp.cadastros.produto.dominio.UnidadeDeMedidaRepositorio;
import br.com.goerp.compartilhado.aplicacao.CasoDeUso;

/** Inativar impede o uso em novos produtos; os que já usam a unidade continuam como estão. */
@CasoDeUso
public class AlterarSituacaoDaUnidadeDeMedida {

    private final UnidadeDeMedidaRepositorio unidades;
    private final BuscarUnidadeDeMedida buscar;

    public AlterarSituacaoDaUnidadeDeMedida(UnidadeDeMedidaRepositorio unidades, BuscarUnidadeDeMedida buscar) {
        this.unidades = unidades;
        this.buscar = buscar;
    }

    public UnidadeDeMedida executar(Long id, boolean ativar) {
        UnidadeDeMedida unidade = buscar.executar(id);
        unidade.definirSituacao(ativar);
        return unidades.salvar(unidade);
    }
}
