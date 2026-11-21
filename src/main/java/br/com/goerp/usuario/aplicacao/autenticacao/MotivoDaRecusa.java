package br.com.goerp.usuario.aplicacao.autenticacao;

public enum MotivoDaRecusa {
    CREDENCIAIS_INVALIDAS("E-mail ou senha inválidos"),
    USUARIO_BLOQUEADO("Usuário bloqueado temporariamente por excesso de tentativas"),
    USUARIO_INATIVO("Usuário inativo"),
    SEM_ACESSO_A_EMPRESA("Usuário sem acesso à empresa informada"),
    SESSAO_INVALIDA("Sessão inválida ou expirada; faça login novamente");

    private final String mensagem;

    MotivoDaRecusa(String mensagem) {
        this.mensagem = mensagem;
    }

    public String mensagem() {
        return mensagem;
    }
}
