package br.com.goerp.usuario.aplicacao.autenticacao;

import java.time.Instant;

/** Porta para o formato do token de acesso (JWT, na infraestrutura). */
public interface GeradorDeTokenDeAcesso {

    TokenDeAcesso gerar(DadosDoTokenDeAcesso dados, Instant emitidoEm);
}
