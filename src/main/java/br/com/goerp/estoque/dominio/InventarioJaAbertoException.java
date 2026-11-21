package br.com.goerp.estoque.dominio;

import br.com.goerp.compartilhado.dominio.excecao.ExcecaoDeDominio;

public class InventarioJaAbertoException extends ExcecaoDeDominio {

    public InventarioJaAbertoException() {
        super("Já existe um inventário aberto; finalize ou cancele antes de abrir outro");
    }
}
