package br.com.goerp.vendas.dominio;

import br.com.goerp.compartilhado.dominio.excecao.ExcecaoDeDominio;

public class DescontoNaoPermitidoException extends ExcecaoDeDominio {

    public DescontoNaoPermitidoException() {
        super("Você não tem permissão para dar desconto");
    }
}
