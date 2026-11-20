package br.com.goerp.empresa.aplicacao;

import br.com.goerp.compartilhado.aplicacao.CasoDeUso;
import br.com.goerp.empresa.dominio.Empresa;
import br.com.goerp.empresa.dominio.EmpresaRepositorio;

@CasoDeUso
public class ReativarEmpresa {

    private final EmpresaRepositorio empresas;
    private final BuscarEmpresa buscarEmpresa;

    public ReativarEmpresa(EmpresaRepositorio empresas, BuscarEmpresa buscarEmpresa) {
        this.empresas = empresas;
        this.buscarEmpresa = buscarEmpresa;
    }

    public Empresa executar(Long id) {
        Empresa empresa = buscarEmpresa.executar(id);
        empresa.ativar();
        return empresas.salvar(empresa);
    }
}
