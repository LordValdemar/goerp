package br.com.goerp.estoque.dominio;

import br.com.goerp.compartilhado.dominio.excecao.ExcecaoDeDominio;

public class InventarioSemContagemException extends ExcecaoDeDominio {

    public InventarioSemContagemException() {
        super("Registre a contagem de pelo menos um produto antes de finalizar");
    }
}
