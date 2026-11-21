package br.com.goerp.compras.dominio;

import br.com.goerp.compartilhado.dominio.excecao.ExcecaoDeDominio;

public class RecebimentoJaEstornadoException extends ExcecaoDeDominio {

    public RecebimentoJaEstornadoException() {
        super("Este recebimento já foi estornado");
    }
}
