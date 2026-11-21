package br.com.goerp.vendas.dominio;

import br.com.goerp.compartilhado.dominio.excecao.ExcecaoDeDominio;

public class CaixaNaoAbertoException extends ExcecaoDeDominio {

    public CaixaNaoAbertoException() {
        super("Abra o seu caixa antes de vender");
    }
}
