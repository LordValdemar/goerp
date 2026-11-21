package br.com.goerp.usuario.dominio;

import br.com.goerp.compartilhado.dominio.excecao.ExcecaoDeDominio;

public class PerfilInativoException extends ExcecaoDeDominio {

    public PerfilInativoException() {
        super("O perfil escolhido está inativo");
    }
}
