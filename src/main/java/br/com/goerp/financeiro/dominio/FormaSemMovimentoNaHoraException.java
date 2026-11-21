package br.com.goerp.financeiro.dominio;

import br.com.goerp.compartilhado.dominio.excecao.ExcecaoDeDominio;

public class FormaSemMovimentoNaHoraException extends ExcecaoDeDominio {

    public FormaSemMovimentoNaHoraException() {
        super("A prazo não movimenta dinheiro agora: gera um título, não um lançamento");
    }
}
