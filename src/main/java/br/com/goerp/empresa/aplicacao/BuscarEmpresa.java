package br.com.goerp.empresa.aplicacao;

import br.com.goerp.compartilhado.aplicacao.CasoDeUso;
import br.com.goerp.compartilhado.dominio.excecao.RecursoNaoEncontradoException;
import br.com.goerp.empresa.dominio.Empresa;
import br.com.goerp.empresa.dominio.EmpresaRepositorio;

@CasoDeUso
public class BuscarEmpresa {

    private final EmpresaRepositorio empresas;

    public BuscarEmpresa(EmpresaRepositorio empresas) {
        this.empresas = empresas;
    }

    public Empresa executar(Long id) {
        return empresas.buscarPorId(id).orElseThrow(() -> new RecursoNaoEncontradoException("Empresa", id));
    }
}
