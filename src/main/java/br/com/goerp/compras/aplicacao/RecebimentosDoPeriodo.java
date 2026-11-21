package br.com.goerp.compras.aplicacao;

import java.util.List;
import java.util.Map;

import br.com.goerp.compartilhado.dominio.Dinheiro;
import br.com.goerp.compras.dominio.Recebimento;

/** Recebimentos de um período (sem os estornados, salvo se pedidos), com totais. */
public record RecebimentosDoPeriodo(List<Linha> recebimentos, Dinheiro total, Map<Long, Dinheiro> totalPorFornecedor) {

    /** O recebimento com o fornecedor e o número do pedido de compra. */
    public record Linha(Recebimento recebimento, Long fornecedorId, Long numeroDoPedido) {
    }
}
