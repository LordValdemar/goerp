package br.com.goerp.empresa.aplicacao;

import java.util.List;

import br.com.goerp.compartilhado.aplicacao.CasoDeUso;
import br.com.goerp.empresa.dominio.Empresa;
import br.com.goerp.empresa.dominio.EmpresaRepositorio;

@CasoDeUso
public class ListarEmpresas {

    private final EmpresaRepositorio empresas;

    public ListarEmpresas(EmpresaRepositorio empresas) {
        this.empresas = empresas;
    }

    public List<Empresa> executar() {
        return empresas.listarTodas();
    }
}
