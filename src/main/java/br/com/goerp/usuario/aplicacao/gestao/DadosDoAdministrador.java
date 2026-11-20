package br.com.goerp.usuario.aplicacao.gestao;

/** @param nome e senha usados só se o e-mail ainda não existir no sistema */
public record DadosDoAdministrador(String nome, String email, String senha) {

    @Override
    public String toString() {
        return "DadosDoAdministrador[nome=" + nome + ", email=" + email + "]";
    }
}
