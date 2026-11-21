package br.com.goerp.vendas.aplicacao;

import java.util.List;

import br.com.goerp.compartilhado.dominio.Cpf;
import br.com.goerp.compartilhado.dominio.Dinheiro;
import br.com.goerp.compartilhado.dominio.Quantidade;
import br.com.goerp.vendas.dominio.Pagamento;

/** O que o PDV envia: o carrinho inteiro e os pagamentos, de uma vez. */
public record SolicitacaoDeVenda(
        List<ItemSolicitado> itens,
        Dinheiro descontoNaVenda,
        List<Pagamento> pagamentos,
        Long clienteId,
        Cpf cpfNaNota) {

    /** O produto pode vir pelo id ou pelo código de barras lido no caixa. */
    public record ItemSolicitado(Long produtoId, String codigoDeBarras, Quantidade quantidade, Dinheiro desconto) {
    }
}
