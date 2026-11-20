package br.com.goerp.usuario.web;

import java.util.Set;

import br.com.goerp.usuario.aplicacao.perfil.DadosDoPerfil;
import br.com.goerp.usuario.dominio.Permissao;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

public record PerfilRequest(@NotBlank @Size(max = 80) String nome, @NotEmpty Set<Permissao> permissoes) {

    DadosDoPerfil paraDados() {
        return new DadosDoPerfil(nome, permissoes);
    }
}
