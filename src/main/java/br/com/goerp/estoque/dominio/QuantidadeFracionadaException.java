package br.com.goerp.estoque.dominio;

import br.com.goerp.compartilhado.dominio.excecao.ExcecaoDeDominio;

public class QuantidadeFracionadaException extends ExcecaoDeDominio {

    public QuantidadeFracionadaException() {
        super("A unidade deste produto não aceita quantidade fracionada");
    }
}
