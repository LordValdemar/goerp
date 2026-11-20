package br.com.goerp.compartilhado.infraestrutura.seguranca;

import org.springframework.stereotype.Component;

import br.com.goerp.compartilhado.aplicacao.EmpresaAtual;

@Component
public class EmpresaDoToken implements EmpresaAtual {

    @Override
    public Long id() {
        return UsuarioAutenticado.idEmpresa()
                .orElseThrow(() -> new IllegalStateException("Nenhum usuário autenticado em uma empresa"));
    }
}
