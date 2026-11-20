package br.com.goerp.usuario.web;

import br.com.goerp.usuario.aplicacao.gestao.DadosDoNovoUsuario;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** Nome e senha só são usados se o e-mail ainda não estiver cadastrado no sistema. */
public record NovoUsuarioRequest(
        @Size(max = 150) String nome,
        @NotBlank String email,
        String senha,
        @NotNull Long perfilId) {

    DadosDoNovoUsuario paraDados() {
        return new DadosDoNovoUsuario(nome, email, senha, perfilId);
    }

    @Override
    public String toString() {
        return "NovoUsuarioRequest[nome=" + nome + ", email=" + email + ", perfilId=" + perfilId + "]";
    }
}
