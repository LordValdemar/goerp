package br.com.goerp.usuario.aplicacao.perfil;

import java.util.Set;

import br.com.goerp.usuario.dominio.Permissao;

public record DadosDoPerfil(String nome, Set<Permissao> permissoes) {
}
