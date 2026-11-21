package br.com.goerp.plataforma.web;

import br.com.goerp.empresa.aplicacao.DadosDaEmpresa;
import br.com.goerp.plataforma.aplicacao.NovaEmpresa;
import br.com.goerp.usuario.aplicacao.gestao.DadosDoAdministrador;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record NovaEmpresaRequest(
        @NotBlank String cnpj,
        @NotBlank @Size(max = 150) String razaoSocial,
        @Size(max = 150) String nomeFantasia,
        @NotNull @Valid Administrador administrador) {

    /** Nome e senha só são usados se o e-mail ainda não existir no sistema. */
    public record Administrador(@Size(max = 150) String nome, @NotBlank String email, String senha) {

        @Override
        public String toString() {
            return "Administrador[nome=" + nome + ", email=" + email + "]";
        }
    }

    NovaEmpresa paraDados() {
        return new NovaEmpresa(new DadosDaEmpresa(cnpj, razaoSocial, nomeFantasia),
                new DadosDoAdministrador(administrador.nome(), administrador.email(), administrador.senha()));
    }
}
