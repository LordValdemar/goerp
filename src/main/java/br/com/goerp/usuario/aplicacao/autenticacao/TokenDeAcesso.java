package br.com.goerp.usuario.aplicacao.autenticacao;

import java.time.Instant;

/** Token de curta duração enviado em cada requisição (cabeçalho Authorization: Bearer). */
public record TokenDeAcesso(String valor, Instant expiraEm) {
}
