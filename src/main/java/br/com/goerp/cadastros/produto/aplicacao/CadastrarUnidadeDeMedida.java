package br.com.goerp.cadastros.produto.aplicacao;

import br.com.goerp.cadastros.produto.dominio.UnidadeDeMedida;
import br.com.goerp.cadastros.produto.dominio.UnidadeDeMedidaRepositorio;
import br.com.goerp.cadastros.produto.dominio.UnidadeJaCadastradaException;
import br.com.goerp.compartilhado.aplicacao.CasoDeUso;
import br.com.goerp.compartilhado.aplicacao.EmpresaAtual;

@CasoDeUso
public class CadastrarUnidadeDeMedida {

    private final UnidadeDeMedidaRepositorio unidades;
    private final EmpresaAtual empresaAtual;

    public CadastrarUnidadeDeMedida(UnidadeDeMedidaRepositorio unidades, EmpresaAtual empresaAtual) {
        this.unidades = unidades;
        this.empresaAtual = empresaAtual;
    }

    public UnidadeDeMedida executar(String sigla, String descricao, boolean fracionavel) {
        UnidadeDeMedida unidade = UnidadeDeMedida.nova(empresaAtual.id(), sigla, descricao, fracionavel);
        if (unidades.existeComSigla(unidade.sigla())) {
            throw new UnidadeJaCadastradaException(unidade.sigla());
        }
        return unidades.salvar(unidade);
    }
}
