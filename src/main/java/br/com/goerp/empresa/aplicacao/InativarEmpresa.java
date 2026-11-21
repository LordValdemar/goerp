package br.com.goerp.empresa.aplicacao;

import br.com.goerp.compartilhado.aplicacao.CasoDeUso;
import br.com.goerp.empresa.dominio.Empresa;
import br.com.goerp.empresa.dominio.EmpresaRepositorio;

/**
 * Empresa inativa: ninguém consegue entrar nela nem renovar sessões abertas.
 * Os dados são mantidos (nunca apagamos registros de negócio).
 */
@CasoDeUso
public class InativarEmpresa {

    private final EmpresaRepositorio empresas;
    private final BuscarEmpresa buscarEmpresa;

    public InativarEmpresa(EmpresaRepositorio empresas, BuscarEmpresa buscarEmpresa) {
        this.empresas = empresas;
        this.buscarEmpresa = buscarEmpresa;
    }

    public void executar(Long id) {
        Empresa empresa = buscarEmpresa.executar(id);
        empresa.inativar();
        empresas.salvar(empresa);
    }
}
