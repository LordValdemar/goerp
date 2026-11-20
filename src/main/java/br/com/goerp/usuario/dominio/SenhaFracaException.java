package br.com.goerp.usuario.dominio;

import br.com.goerp.compartilhado.dominio.excecao.ExcecaoDeDominio;

public class SenhaFracaException extends ExcecaoDeDominio {

    public SenhaFracaException() {
        super("A senha deve ter entre " + Senha.TAMANHO_MINIMO + " e " + Senha.TAMANHO_MAXIMO
                + " caracteres, com pelo menos uma letra e um número");
    }
}
