package br.com.goerp.financeiro.dominio;

import static br.com.goerp.compartilhado.dominio.Textos.obrigatorio;

import java.util.Objects;

import br.com.goerp.compartilhado.dominio.Dinheiro;
import br.com.goerp.compartilhado.dominio.excecao.ValorInvalidoException;

/**
 * O que se quer lançar numa conta financeira.
 *
 * @param descricao          histórico do lançamento (no estorno, o motivo)
 * @param categoriaId        opcional
 * @param documentoDeOrigem  id do título, venda ou transferência que gerou o lançamento (opcional)
 * @param estornado          id do lançamento estornado (só em estornos)
 */
public record PedidoDeLancamento(
        TipoDeLancamento tipo,
        Dinheiro valor,
        FormaDePagamento forma,
        Long categoriaId,
        String descricao,
        OrigemDoLancamento origem,
        Long documentoDeOrigem,
        Long estornado,
        Long usuarioId) {

    public PedidoDeLancamento {
        Objects.requireNonNull(tipo, "tipo");
        Objects.requireNonNull(valor, "valor");
        Objects.requireNonNull(forma, "forma");
        origem = Objects.requireNonNullElse(origem, OrigemDoLancamento.MANUAL);
        descricao = obrigatorio(descricao, "descricao");
        if (valor.compareTo(Dinheiro.ZERO) <= 0) {
            throw new ValorInvalidoException("O valor do lançamento precisa ser maior que zero");
        }
        if (!forma.movimentaNaHora()) {
            throw new FormaSemMovimentoNaHoraException();
        }
        if (tipo.ehEstorno() != (estornado != null)) {
            throw new IllegalArgumentException("Estorno precisa indicar o lançamento estornado (e só estorno)");
        }
    }
}
