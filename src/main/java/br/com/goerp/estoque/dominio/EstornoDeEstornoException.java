package br.com.goerp.estoque.dominio;

import br.com.goerp.compartilhado.dominio.excecao.ExcecaoDeDominio;

public class EstornoDeEstornoException extends ExcecaoDeDominio {

    public EstornoDeEstornoException() {
        super("Um estorno não pode ser estornado; faça uma nova movimentação");
    }
}
