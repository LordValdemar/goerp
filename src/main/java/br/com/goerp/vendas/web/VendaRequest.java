package br.com.goerp.vendas.web;

import java.math.BigDecimal;
import java.util.List;

import br.com.goerp.compartilhado.dominio.Cpf;
import br.com.goerp.compartilhado.dominio.Dinheiro;
import br.com.goerp.compartilhado.dominio.Quantidade;
import br.com.goerp.vendas.aplicacao.ItemSolicitado;
import br.com.goerp.vendas.aplicacao.SolicitacaoDeVenda;
import br.com.goerp.vendas.dominio.FormaDePagamento;
import br.com.goerp.vendas.dominio.Pagamento;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

/** O carrinho inteiro e os pagamentos. Não há preço: ele vem do cadastro. */
public record VendaRequest(
        @NotEmpty List<@Valid Item> itens,
        BigDecimal descontoNaVenda,
        @NotEmpty List<@Valid PagamentoRequest> pagamentos,
        Long clienteId,
        String cpfNaNota) {

    /** Informe {@code produtoId} ou {@code codigoDeBarras}. */
    public record Item(Long produtoId, String codigoDeBarras, @NotNull BigDecimal quantidade, BigDecimal desconto) {
    }

    public record PagamentoRequest(@NotNull FormaDePagamento forma, @NotNull BigDecimal valor) {
    }

    SolicitacaoDeVenda paraSolicitacao() {
        return new SolicitacaoDeVenda(
                itens.stream().map(item -> new ItemSolicitado(item.produtoId(),
                        item.codigoDeBarras(), new Quantidade(item.quantidade()),
                        item.desconto() == null ? null : Dinheiro.de(item.desconto()))).toList(),
                descontoNaVenda == null ? null : Dinheiro.de(descontoNaVenda),
                pagamentos.stream().map(p -> new Pagamento(p.forma(), Dinheiro.de(p.valor()))).toList(),
                clienteId,
                cpfNaNota == null || cpfNaNota.isBlank() ? null : Cpf.de(cpfNaNota));
    }
}
