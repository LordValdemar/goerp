package br.com.goerp.usuario.web;

import br.com.goerp.usuario.aplicacao.gestao.UsuarioDaEmpresa;

/** @param ativo se o acesso a ESTA empresa está ativo */
public record UsuarioDaEmpresaResponse(Long id, String nome, String email, boolean ativo, PerfilResumo perfil) {

    public record PerfilResumo(Long id, String nome) {
    }

    static UsuarioDaEmpresaResponse de(UsuarioDaEmpresa usuarioDaEmpresa) {
        var usuario = usuarioDaEmpresa.usuario();
        var perfil = usuarioDaEmpresa.perfil();
        return new UsuarioDaEmpresaResponse(usuario.id(), usuario.nome(), usuario.email().valor(),
                usuarioDaEmpresa.vinculo().ativo(), new PerfilResumo(perfil.id(), perfil.nome()));
    }
}
