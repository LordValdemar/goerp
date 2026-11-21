package br.com.goerp.plataforma.web;

import br.com.goerp.plataforma.aplicacao.EmpresaCadastrada;

public record EmpresaCadastradaResponse(EmpresaResponse empresa, Administrador administrador) {

    /** @param usuarioJaExistia o administrador já tinha login e continua com a senha dele */
    public record Administrador(Long id, String nome, String email, boolean usuarioJaExistia) {
    }

    static EmpresaCadastradaResponse de(EmpresaCadastrada cadastrada) {
        var acesso = cadastrada.acessoInicial();
        var usuario = acesso.administrador();
        return new EmpresaCadastradaResponse(EmpresaResponse.de(cadastrada.empresa()),
                new Administrador(usuario.id(), usuario.nome(), usuario.email().valor(), acesso.usuarioJaExistia()));
    }
}
