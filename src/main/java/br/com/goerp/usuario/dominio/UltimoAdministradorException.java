package br.com.goerp.usuario.dominio;

import br.com.goerp.compartilhado.dominio.excecao.ExcecaoDeDominio;

public class UltimoAdministradorException extends ExcecaoDeDominio {

    public UltimoAdministradorException() {
        super("A empresa precisa de pelo menos um usuário ativo com o perfil Administrador");
    }
}
