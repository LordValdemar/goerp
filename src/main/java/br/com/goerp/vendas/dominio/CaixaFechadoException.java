package br.com.goerp.vendas.dominio;

import br.com.goerp.compartilhado.dominio.excecao.ExcecaoDeDominio;

public class CaixaFechadoException extends ExcecaoDeDominio {

    public CaixaFechadoException() {
        super("Este caixa já foi fechado");
    }
}
