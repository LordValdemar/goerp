package br.com.goerp.usuario.dominio;

import br.com.goerp.compartilhado.dominio.excecao.ExcecaoDeDominio;

public class AutoInativacaoException extends ExcecaoDeDominio {

    public AutoInativacaoException() {
        super("Você não pode inativar o seu próprio acesso");
    }
}
