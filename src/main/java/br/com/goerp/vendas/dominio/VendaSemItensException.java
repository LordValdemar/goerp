package br.com.goerp.vendas.dominio;

import br.com.goerp.compartilhado.dominio.excecao.ExcecaoDeDominio;

public class VendaSemItensException extends ExcecaoDeDominio {

    public VendaSemItensException() {
        super("A venda precisa de pelo menos um item");
    }
}
