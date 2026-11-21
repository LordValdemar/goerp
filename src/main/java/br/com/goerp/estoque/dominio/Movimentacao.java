package br.com.goerp.estoque.dominio;

import java.time.Instant;
import java.util.Objects;

import br.com.goerp.cadastros.produto.dominio.CustoUnitario;
import br.com.goerp.compartilhado.dominio.Quantidade;

/**
 * Um lançamento no estoque. É imutável: nunca é alterado nem apagado. Para corrigir, estorna-se.
 * Guarda o saldo e o custo médio logo após ele, o que permite montar o extrato sem recalcular.
 */
public record Movimentacao(
        Long id,
        Long empresaId,
        Long produtoId,
        TipoDeMovimentacao tipo,
        Quantidade quantidade,
        CustoUnitario custoUnitario,
        Quantidade saldoResultante,
        CustoUnitario custoMedioResultante,
        OrigemDaMovimentacao origem,
        Long documentoDeOrigem,
        String motivo,
        Long estornada,
        Long usuarioId,
        Instant momento) {

    public Movimentacao {
        Objects.requireNonNull(empresaId, "empresaId");
        Objects.requireNonNull(produtoId, "produtoId");
        Objects.requireNonNull(tipo, "tipo");
        Objects.requireNonNull(quantidade, "quantidade");
        Objects.requireNonNull(custoUnitario, "custoUnitario");
        Objects.requireNonNull(saldoResultante, "saldoResultante");
        Objects.requireNonNull(custoMedioResultante, "custoMedioResultante");
        Objects.requireNonNull(momento, "momento");
    }

    /** Quantidade com sinal: positiva se somou ao saldo, negativa se subtraiu. */
    public Quantidade efeitoNoSaldo() {
        return tipo.soma() ? quantidade : Quantidade.ZERO.subtrair(quantidade);
    }

    /** Pedido que desfaz esta movimentação, ao mesmo custo dela. */
    public PedidoDeMovimentacao pedidoDeEstorno(String motivoDoEstorno, Long usuario) {
        return new PedidoDeMovimentacao(tipo.estorno(), quantidade, custoUnitario, origem, documentoDeOrigem,
                motivoDoEstorno, id, usuario);
    }
}
