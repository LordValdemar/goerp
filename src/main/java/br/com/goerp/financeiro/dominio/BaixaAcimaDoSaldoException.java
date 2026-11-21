package br.com.goerp.financeiro.dominio;

import br.com.goerp.compartilhado.dominio.excecao.ExcecaoDeDominio;
import br.com.goerp.compartilhado.dominio.Dinheiro;

public class BaixaAcimaDoSaldoException extends ExcecaoDeDominio {

    public BaixaAcimaDoSaldoException(Dinheiro saldoDevedor, Dinheiro amortizado) {
        super("O pagamento abate " + amortizado + ", mas o título só deve " + saldoDevedor);
    }
}
