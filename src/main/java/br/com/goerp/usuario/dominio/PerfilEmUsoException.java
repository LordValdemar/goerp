package br.com.goerp.usuario.dominio;

import br.com.goerp.compartilhado.dominio.excecao.ExcecaoDeDominio;

public class PerfilEmUsoException extends ExcecaoDeDominio {

    public PerfilEmUsoException() {
        super("O perfil está em uso por usuários ativos; troque o perfil deles antes de inativá-lo");
    }
}
