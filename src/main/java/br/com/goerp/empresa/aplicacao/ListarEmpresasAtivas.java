package br.com.goerp.empresa.aplicacao;

import java.util.Collection;
import java.util.List;

import br.com.goerp.compartilhado.aplicacao.CasoDeUso;
import br.com.goerp.empresa.dominio.Empresa;
import br.com.goerp.empresa.dominio.EmpresaRepositorio;

/** Entre as empresas informadas, devolve apenas as ativas, ordenadas pela razão social. */
@CasoDeUso
public class ListarEmpresasAtivas {

    private final EmpresaRepositorio empresas;

    public ListarEmpresasAtivas(EmpresaRepositorio empresas) {
        this.empresas = empresas;
    }

    public List<Empresa> executar(Collection<Long> ids) {
        return empresas.listarAtivasPorIds(ids);
    }
}
