package br.com.goerp.usuario.dominio;

import br.com.goerp.compartilhado.dominio.excecao.ExcecaoDeDominio;

public class PerfilDoSistemaException extends ExcecaoDeDominio {

    public PerfilDoSistemaException() {
        super("O perfil Administrador é do sistema e não pode ser alterado nem inativado");
    }
}
