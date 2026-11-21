package br.com.goerp.empresa.aplicacao;

import br.com.goerp.compartilhado.aplicacao.CasoDeUso;
import br.com.goerp.empresa.dominio.Empresa;
import br.com.goerp.empresa.dominio.EmpresaRepositorio;

/** O CNPJ identifica a empresa e não muda; só razão social e nome fantasia. */
@CasoDeUso
public class AlterarDadosDaEmpresa {

    private final EmpresaRepositorio empresas;
    private final BuscarEmpresa buscarEmpresa;

    public AlterarDadosDaEmpresa(EmpresaRepositorio empresas, BuscarEmpresa buscarEmpresa) {
        this.empresas = empresas;
        this.buscarEmpresa = buscarEmpresa;
    }

    public Empresa executar(Long id, String razaoSocial, String nomeFantasia) {
        Empresa empresa = buscarEmpresa.executar(id);
        empresa.alterarDados(razaoSocial, nomeFantasia);
        return empresas.salvar(empresa);
    }
}
