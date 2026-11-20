package br.com.goerp.usuario.aplicacao.gestao;

/**
 * @param nome  usado só se o e-mail ainda não existir no sistema
 * @param senha idem; um usuário já existente mantém a própria senha
 */
public record DadosDoNovoUsuario(String nome, String email, String senha, Long perfilId) {

    @Override
    public String toString() {
        return "DadosDoNovoUsuario[nome=" + nome + ", email=" + email + ", perfilId=" + perfilId + "]";
    }
}
