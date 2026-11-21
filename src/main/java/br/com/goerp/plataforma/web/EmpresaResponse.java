package br.com.goerp.plataforma.web;

import br.com.goerp.empresa.dominio.Empresa;

public record EmpresaResponse(Long id, String cnpj, String razaoSocial, String nomeFantasia, boolean ativa) {

    static EmpresaResponse de(Empresa empresa) {
        return new EmpresaResponse(empresa.id(), empresa.cnpj().formatado(), empresa.razaoSocial(),
                empresa.nomeFantasia(), empresa.ativa());
    }
}
