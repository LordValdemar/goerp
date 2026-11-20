package br.com.goerp.usuario.dominio;

import br.com.goerp.compartilhado.dominio.excecao.ExcecaoDeDominio;

public class PerfilJaExisteException extends ExcecaoDeDominio {

    public PerfilJaExisteException(String nome) {
        super("Já existe um perfil com o nome '" + nome + "'");
    }
}
