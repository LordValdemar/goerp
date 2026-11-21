package br.com.goerp.usuario.aplicacao.autenticacao;

import java.util.Set;

/** O que vai dentro do token de acesso. {@code empresaId} é nulo para o administrador da plataforma. */
public record DadosDoTokenDeAcesso(Long usuarioId, String nome, Long empresaId, Set<String> permissoes) {

    public DadosDoTokenDeAcesso {
        permissoes = Set.copyOf(permissoes);
    }
}
