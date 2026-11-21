package br.com.goerp.compras.dominio;

import java.util.Objects;

import br.com.goerp.cadastros.produto.dominio.CustoUnitario;
import br.com.goerp.compartilhado.dominio.Dinheiro;
import br.com.goerp.compartilhado.dominio.Quantidade;
import br.com.goerp.compartilhado.dominio.excecao.ValorInvalidoException;

/** O que chegou de um produto e a que custo (o da nota do fornecedor, que entra no custo médio). */
public record ItemRecebido(Long produtoId, Quantidade quantidade, CustoUnitario custoUnitario) {

    public ItemRecebido {
        Objects.requireNonNull(produtoId, "produtoId");
        Objects.requireNonNull(quantidade, "quantidade");
        Objects.requireNonNull(custoUnitario, "custoUnitario");
        if (quantidade.compareTo(Quantidade.ZERO) <= 0) {
            throw new ValorInvalidoException("A quantidade recebida precisa ser maior que zero");
        }
    }

    public Dinheiro valor() {
        return Dinheiro.de(quantidade.valor().multiply(custoUnitario.valor())
                .setScale(Dinheiro.CASAS_DECIMAIS, Dinheiro.ARREDONDAMENTO));
    }
}
