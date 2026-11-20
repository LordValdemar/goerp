package br.com.goerp.estoque.dominio;

import br.com.goerp.compartilhado.dominio.excecao.ExcecaoDeDominio;

public class MovimentacaoJaEstornadaException extends ExcecaoDeDominio {

    public MovimentacaoJaEstornadaException() {
        super("Esta movimentação já foi estornada");
    }
}
