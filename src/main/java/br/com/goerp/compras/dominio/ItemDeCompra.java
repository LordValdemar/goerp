package br.com.goerp.compras.dominio;

import static br.com.goerp.compartilhado.dominio.Textos.obrigatorio;

import java.util.Objects;

import br.com.goerp.cadastros.produto.dominio.CustoUnitario;
import br.com.goerp.compartilhado.dominio.Dinheiro;
import br.com.goerp.compartilhado.dominio.Quantidade;
import br.com.goerp.compartilhado.dominio.excecao.ValorInvalidoException;

/**
 * Item do pedido: quanto foi pedido, ao custo negociado (4 casas), e quanto já chegou.
 * Código e descrição são copiados do cadastro na hora do pedido.
 */
public record ItemDeCompra(
        Long produtoId,
        String codigo,
        String descricao,
        Quantidade quantidade,
        CustoUnitario custoNegociado,
        Quantidade recebida) {

    public ItemDeCompra {
        Objects.requireNonNull(produtoId, "produtoId");
        codigo = obrigatorio(codigo, "codigo");
        descricao = obrigatorio(descricao, "descricao");
        Objects.requireNonNull(quantidade, "quantidade");
        Objects.requireNonNull(custoNegociado, "custoNegociado");
        recebida = Objects.requireNonNullElse(recebida, Quantidade.ZERO);
        if (quantidade.compareTo(Quantidade.ZERO) <= 0) {
            throw new ValorInvalidoException("A quantidade pedida precisa ser maior que zero");
        }
        if (recebida.negativa() || recebida.compareTo(quantidade) > 0) {
            throw new IllegalArgumentException("Quantidade recebida fora do intervalo do pedido");
        }
    }

    public static ItemDeCompra novo(Long produtoId, String codigo, String descricao, Quantidade quantidade,
            CustoUnitario custoNegociado) {
        return new ItemDeCompra(produtoId, codigo, descricao, quantidade, custoNegociado, Quantidade.ZERO);
    }

    public Quantidade pendente() {
        return quantidade.subtrair(recebida);
    }

    public boolean completo() {
        return pendente().compareTo(Quantidade.ZERO) == 0;
    }

    /** Valor do item ao custo negociado, arredondado a centavos. */
    public Dinheiro valor() {
        return Dinheiro.de(quantidade.valor().multiply(custoNegociado.valor())
                .setScale(Dinheiro.CASAS_DECIMAIS, Dinheiro.ARREDONDAMENTO));
    }

    /** Valor do que ainda falta chegar, ao custo negociado. */
    public Dinheiro valorPendente() {
        return Dinheiro.de(pendente().valor().multiply(custoNegociado.valor())
                .setScale(Dinheiro.CASAS_DECIMAIS, Dinheiro.ARREDONDAMENTO));
    }

    ItemDeCompra receber(Quantidade quantidadeRecebida) {
        if (quantidadeRecebida.compareTo(pendente()) > 0) {
            throw new QuantidadeAcimaDoPendenteException(codigo, pendente());
        }
        return new ItemDeCompra(produtoId, codigo, descricao, quantidade, custoNegociado,
                recebida.somar(quantidadeRecebida));
    }

    ItemDeCompra desfazerRecebimento(Quantidade quantidadeEstornada) {
        return new ItemDeCompra(produtoId, codigo, descricao, quantidade, custoNegociado,
                recebida.subtrair(quantidadeEstornada));
    }
}
