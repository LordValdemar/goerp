package br.com.goerp.financeiro.dominio;

import br.com.goerp.compartilhado.dominio.excecao.ExcecaoDeDominio;
import br.com.goerp.compartilhado.dominio.Dinheiro;

public class SaldoInsuficienteException extends ExcecaoDeDominio {

    public SaldoInsuficienteException(Dinheiro disponivel, Dinheiro pedido) {
        super("Saldo insuficiente na conta: disponível " + disponivel + ", pedido " + pedido);
    }
}
