package br.com.goerp.vendas.dominio;

import br.com.goerp.compartilhado.dominio.excecao.ExcecaoDeDominio;

public class CaixaJaAbertoException extends ExcecaoDeDominio {

    public CaixaJaAbertoException() {
        super("Você já tem um caixa aberto; feche-o antes de abrir outro");
    }
}
