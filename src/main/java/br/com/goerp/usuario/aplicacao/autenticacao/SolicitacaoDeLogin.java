package br.com.goerp.usuario.aplicacao.autenticacao;

/** @param empresaId opcional: obrigatório só quando o usuário tem acesso a mais de uma empresa */
public record SolicitacaoDeLogin(String email, String senha, Long empresaId) {

    @Override
    public String toString() {
        return "SolicitacaoDeLogin[email=" + email + ", empresaId=" + empresaId + "]";
    }
}
