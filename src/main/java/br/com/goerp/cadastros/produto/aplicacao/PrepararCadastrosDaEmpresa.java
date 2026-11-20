package br.com.goerp.cadastros.produto.aplicacao;

import br.com.goerp.cadastros.produto.dominio.UnidadeDeMedida;
import br.com.goerp.cadastros.produto.dominio.UnidadeDeMedidaRepositorio;
import br.com.goerp.compartilhado.aplicacao.AcessoEntreEmpresas;
import br.com.goerp.compartilhado.aplicacao.CasoDeUso;

/**
 * Deixa uma empresa recém-cadastrada pronta para cadastrar produtos (unidades de medida padrão).
 * Acessa todas as empresas porque é chamado pelo administrador da plataforma, que não está dentro
 * da empresa; a empresa de destino é informada explicitamente.
 */
@CasoDeUso
@AcessoEntreEmpresas
public class PrepararCadastrosDaEmpresa {

    private final UnidadeDeMedidaRepositorio unidades;

    public PrepararCadastrosDaEmpresa(UnidadeDeMedidaRepositorio unidades) {
        this.unidades = unidades;
    }

    public void executar(Long empresaId) {
        UnidadeDeMedida.padroesDa(empresaId).forEach(unidades::salvar);
    }
}
