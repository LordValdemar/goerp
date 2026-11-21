package br.com.goerp.vendas.dominio;

import br.com.goerp.compartilhado.dominio.excecao.ExcecaoDeDominio;

public class PagamentoAcimaDoTotalException extends ExcecaoDeDominio {

    public PagamentoAcimaDoTotalException() {
        super("Só pagamento em dinheiro pode passar do total (gera troco); ajuste os valores dos outros pagamentos");
    }
}
