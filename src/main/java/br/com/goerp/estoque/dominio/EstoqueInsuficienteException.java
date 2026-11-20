package br.com.goerp.estoque.dominio;

import br.com.goerp.compartilhado.dominio.Quantidade;
import br.com.goerp.compartilhado.dominio.excecao.ExcecaoDeDominio;

public class EstoqueInsuficienteException extends ExcecaoDeDominio {

    public EstoqueInsuficienteException(Quantidade disponivel, Quantidade pedida) {
        super("Estoque insuficiente: disponível " + disponivel.valor().stripTrailingZeros().toPlainString()
                + ", pedido " + pedida.valor().stripTrailingZeros().toPlainString());
    }
}
