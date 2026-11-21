package br.com.goerp.financeiro.dominio;

import br.com.goerp.compartilhado.dominio.excecao.ExcecaoDeDominio;

public class TituloNaoAbertoException extends ExcecaoDeDominio {

    public TituloNaoAbertoException(SituacaoDoTitulo situacao) {
        super("O título está " + situacao.descricao() + " e não aceita esta operação");
    }
}
