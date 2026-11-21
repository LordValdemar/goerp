package br.com.goerp.financeiro.dominio;

import java.time.Instant;
import java.util.Objects;

import br.com.goerp.compartilhado.dominio.Dinheiro;

/**
 * Entrada ou saída de dinheiro numa conta. É imutável: nunca é alterado nem apagado; corrige-se
 * estornando. Guarda o saldo logo após ele, o que permite montar o extrato sem recalcular.
 */
public record Lancamento(
        Long id,
        Long empresaId,
        Long contaId,
        TipoDeLancamento tipo,
        Dinheiro valor,
        Dinheiro saldoResultante,
        FormaDePagamento forma,
        Long categoriaId,
        String descricao,
        OrigemDoLancamento origem,
        Long documentoDeOrigem,
        Long estornado,
        Long usuarioId,
        Instant momento) {

    public Lancamento {
        Objects.requireNonNull(empresaId, "empresaId");
        Objects.requireNonNull(contaId, "contaId");
        Objects.requireNonNull(tipo, "tipo");
        Objects.requireNonNull(valor, "valor");
        Objects.requireNonNull(saldoResultante, "saldoResultante");
        Objects.requireNonNull(forma, "forma");
        Objects.requireNonNull(origem, "origem");
        Objects.requireNonNull(momento, "momento");
    }

    /** Valor com sinal: positivo se entrou na conta, negativo se saiu. */
    public Dinheiro efeitoNoSaldo() {
        return tipo.soma() ? valor : Dinheiro.ZERO.subtrair(valor);
    }

    /** Pedido que desfaz este lançamento, com a mesma forma, categoria e origem. */
    public PedidoDeLancamento pedidoDeEstorno(String motivo, Long usuario) {
        return new PedidoDeLancamento(tipo.estorno(), valor, forma, categoriaId, motivo, origem, documentoDeOrigem, id,
                usuario);
    }
}
