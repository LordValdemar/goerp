package br.com.goerp.vendas.dominio;

import br.com.goerp.compartilhado.dominio.excecao.ExcecaoDeDominio;

public class QuantidadeFracionadaNaVendaException extends ExcecaoDeDominio {

    public QuantidadeFracionadaNaVendaException() {
        super("A unidade deste produto não aceita quantidade fracionada");
    }
}
