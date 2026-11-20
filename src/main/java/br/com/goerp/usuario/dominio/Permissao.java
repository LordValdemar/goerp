package br.com.goerp.usuario.dominio;

/**
 * O que um usuário pode fazer dentro de uma empresa. Cada módulo acrescenta as suas.
 * O nome da constante é o que vai no token e no {@code @PreAuthorize}.
 */
public enum Permissao {
    USUARIO_LER("Consultar usuários da empresa"),
    USUARIO_GERENCIAR("Cadastrar, alterar e inativar usuários da empresa"),
    PERFIL_LER("Consultar perfis de acesso"),
    PERFIL_GERENCIAR("Cadastrar, alterar e inativar perfis de acesso"),
    CLIENTE_LER("Consultar clientes"),
    CLIENTE_GERENCIAR("Cadastrar, alterar e inativar clientes");

    private final String descricao;

    Permissao(String descricao) {
        this.descricao = descricao;
    }

    public String descricao() {
        return descricao;
    }
}
