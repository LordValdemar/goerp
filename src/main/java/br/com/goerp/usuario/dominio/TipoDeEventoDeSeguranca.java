package br.com.goerp.usuario.dominio;

public enum TipoDeEventoDeSeguranca {
    LOGIN_SUCESSO,
    LOGIN_FALHA,
    LOGIN_BLOQUEADO,
    LOGOUT,
    ACESSO_RENOVADO,
    RENOVACAO_RECUSADA,
    TOKEN_REUTILIZADO,
    SENHA_ALTERADA
}
