package br.com.goerp.usuario.dominio;

import br.com.goerp.compartilhado.dominio.excecao.ExcecaoDeDominio;

public class UsuarioJaVinculadoException extends ExcecaoDeDominio {

    public UsuarioJaVinculadoException() {
        super("Este usuário já tem acesso a esta empresa; para liberar um acesso inativo, use a reativação");
    }
}
