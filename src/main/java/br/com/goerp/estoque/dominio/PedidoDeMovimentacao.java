package br.com.goerp.estoque.dominio;

import static br.com.goerp.compartilhado.dominio.Textos.opcional;

import java.util.Objects;

import br.com.goerp.cadastros.produto.dominio.CustoUnitario;
import br.com.goerp.compartilhado.dominio.Quantidade;

/**
 * O que se quer lançar no estoque.
 *
 * @param custoUnitario      só para entradas; nulo usa o custo médio atual
 * @param documentoDeOrigem  id da venda, compra ou inventário que gerou a movimentação (opcional)
 * @param estornada          id da movimentação estornada (só em estornos)
 */
public record PedidoDeMovimentacao(
        TipoDeMovimentacao tipo,
        Quantidade quantidade,
        CustoUnitario custoUnitario,
        OrigemDaMovimentacao origem,
        Long documentoDeOrigem,
        String motivo,
        Long estornada,
        Long usuarioId) {

    public PedidoDeMovimentacao {
        Objects.requireNonNull(tipo, "tipo");
        Objects.requireNonNull(quantidade, "quantidade");
        origem = Objects.requireNonNullElse(origem, OrigemDaMovimentacao.MANUAL);
        motivo = opcional(motivo);
        if (quantidade.compareTo(Quantidade.ZERO) <= 0) {
            throw new QuantidadeInvalidaException();
        }
        if (tipo.exigeMotivo() && motivo == null) {
            throw new MotivoObrigatorioException();
        }
        if (tipo.ehEstorno() != (estornada != null)) {
            throw new IllegalArgumentException("Estorno precisa indicar a movimentação estornada (e só estorno)");
        }
    }
}
