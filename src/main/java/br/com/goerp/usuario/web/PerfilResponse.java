package br.com.goerp.usuario.web;

import java.util.List;

import br.com.goerp.usuario.dominio.Perfil;

public record PerfilResponse(Long id, String nome, List<String> permissoes, boolean administrador, boolean ativo) {

    static PerfilResponse de(Perfil perfil) {
        List<String> permissoes = perfil.permissoes().stream().map(Enum::name).sorted().toList();
        return new PerfilResponse(perfil.id(), perfil.nome(), permissoes, perfil.administrador(), perfil.ativo());
    }
}
