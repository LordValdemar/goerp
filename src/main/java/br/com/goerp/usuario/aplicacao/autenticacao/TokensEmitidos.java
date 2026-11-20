package br.com.goerp.usuario.aplicacao.autenticacao;

import java.time.Instant;

public record TokensEmitidos(TokenDeAcesso acesso, String tokenDeRenovacao, Instant renovacaoExpiraEm) {
}
