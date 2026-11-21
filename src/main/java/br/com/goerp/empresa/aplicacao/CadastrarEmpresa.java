package br.com.goerp.empresa.aplicacao;

import br.com.goerp.compartilhado.aplicacao.CasoDeUso;
import br.com.goerp.compartilhado.dominio.Cnpj;
import br.com.goerp.empresa.dominio.Empresa;
import br.com.goerp.empresa.dominio.EmpresaJaCadastradaException;
import br.com.goerp.empresa.dominio.EmpresaRepositorio;

@CasoDeUso
public class CadastrarEmpresa {

    private final EmpresaRepositorio empresas;

    public CadastrarEmpresa(EmpresaRepositorio empresas) {
        this.empresas = empresas;
    }

    public Empresa executar(DadosDaEmpresa dados) {
        Cnpj cnpj = Cnpj.de(dados.cnpj());
        if (empresas.existeComCnpj(cnpj)) {
            throw new EmpresaJaCadastradaException(cnpj);
        }
        return empresas.salvar(Empresa.nova(cnpj, dados.razaoSocial(), dados.nomeFantasia()));
    }
}
