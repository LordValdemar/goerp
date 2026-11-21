package br.com.goerp.vendas.dominio;

import java.util.Objects;

import br.com.goerp.compartilhado.dominio.Dinheiro;
import br.com.goerp.compartilhado.dominio.excecao.ValorInvalidoException;
import br.com.goerp.financeiro.dominio.FormaDePagamento;

public record Pagamento(FormaDePagamento forma, Dinheiro valor) {

    public Pagamento {
        Objects.requireNonNull(forma, "forma");
        Objects.requireNonNull(valor, "valor");
        if (!forma.conferidaNoCaixa()) {
            throw new FormaNaoAceitaNoPdvException(forma);
        }
        if (valor.compareTo(Dinheiro.ZERO) <= 0) {
            throw new ValorInvalidoException("O valor de cada pagamento precisa ser maior que zero");
        }
    }
}
