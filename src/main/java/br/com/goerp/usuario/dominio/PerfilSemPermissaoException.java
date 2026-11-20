package br.com.goerp.usuario.dominio;

import br.com.goerp.compartilhado.dominio.excecao.ExcecaoDeDominio;

public class PerfilSemPermissaoException extends ExcecaoDeDominio {

    public PerfilSemPermissaoException() {
        super("O perfil precisa de pelo menos uma permissão");
    }
}
