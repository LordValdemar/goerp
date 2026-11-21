package br.com.goerp.financeiro.dominio;

import br.com.goerp.compartilhado.dominio.excecao.ExcecaoDeDominio;

public class TituloComBaixaException extends ExcecaoDeDominio {

    public TituloComBaixaException() {
        super("O título já tem pagamento registrado; estorne os pagamentos antes");
    }
}
