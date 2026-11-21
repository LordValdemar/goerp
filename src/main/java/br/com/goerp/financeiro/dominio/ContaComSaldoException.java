package br.com.goerp.financeiro.dominio;

import br.com.goerp.compartilhado.dominio.excecao.ExcecaoDeDominio;
import br.com.goerp.compartilhado.dominio.Dinheiro;

public class ContaComSaldoException extends ExcecaoDeDominio {

    public ContaComSaldoException(Dinheiro saldo) {
        super("Só é possível inativar a conta com saldo zero (saldo atual: " + saldo + ")");
    }
}
