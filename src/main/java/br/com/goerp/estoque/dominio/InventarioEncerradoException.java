package br.com.goerp.estoque.dominio;

import br.com.goerp.compartilhado.dominio.excecao.ExcecaoDeDominio;

public class InventarioEncerradoException extends ExcecaoDeDominio {

    public InventarioEncerradoException() {
        super("Este inventário já foi finalizado ou cancelado e não pode ser alterado");
    }
}
