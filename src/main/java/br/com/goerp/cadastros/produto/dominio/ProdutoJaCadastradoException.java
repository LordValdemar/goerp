package br.com.goerp.cadastros.produto.dominio;

import br.com.goerp.compartilhado.dominio.excecao.ExcecaoDeDominio;

public class ProdutoJaCadastradoException extends ExcecaoDeDominio {

    public ProdutoJaCadastradoException(String codigo) {
        super("Já existe um produto com o código " + codigo);
    }
}
