package br.com.goerp.financeiro.dominio;

import br.com.goerp.compartilhado.dominio.excecao.ExcecaoDeDominio;

public class BaixaJaEstornadaException extends ExcecaoDeDominio {

    public BaixaJaEstornadaException() {
        super("Este pagamento já foi estornado");
    }
}
