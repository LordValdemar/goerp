package br.com.goerp.usuario.aplicacao.gestao;

import br.com.goerp.usuario.dominio.Usuario;

public record AcessoInicial(Usuario administrador, boolean usuarioJaExistia) {
}
