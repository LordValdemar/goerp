package br.com.goerp.compras.dominio;

import br.com.goerp.compartilhado.dominio.excecao.ExcecaoDeDominio;

public class RecebimentoSemItensException extends ExcecaoDeDominio {

    public RecebimentoSemItensException() {
        super("Informe pelo menos um item recebido");
    }
}
