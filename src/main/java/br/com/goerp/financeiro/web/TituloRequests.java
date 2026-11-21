package br.com.goerp.financeiro.web;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import br.com.goerp.compartilhado.dominio.Dinheiro;
import br.com.goerp.compartilhado.dominio.excecao.CampoObrigatorioException;
import br.com.goerp.financeiro.dominio.FormaDePagamento;
import br.com.goerp.financeiro.dominio.Parcelamento;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

/** Requisições comuns a contas a pagar e a receber. */
final class TituloRequests {

    static final int TAMANHO_DA_DESCRICAO = 150;
    static final int TAMANHO_DO_DOCUMENTO = 60;
    private static final int TAMANHO_DO_MOTIVO = 200;

    private TituloRequests() {
    }

    /**
     * Como dividir o valor: datas escolhidas em {@code vencimentos}, ou {@code parcelas} mensais
     * (padrão 1) a partir do {@code primeiroVencimento}.
     */
    record Vencimentos(@NotNull @Positive BigDecimal valor, LocalDate primeiroVencimento, Integer parcelas,
            List<LocalDate> vencimentos) {

        Parcelamento parcelamento() {
            Dinheiro total = Dinheiro.de(valor);
            if (vencimentos != null && !vencimentos.isEmpty()) {
                return Parcelamento.comVencimentos(total, vencimentos);
            }
            if (primeiroVencimento == null) {
                throw new CampoObrigatorioException("primeiroVencimento");
            }
            return Parcelamento.mensal(total, parcelas == null ? 1 : parcelas, primeiroVencimento);
        }
    }

    /** @param versao a versão que foi lida, para detectar edição simultânea */
    record Alteracao(@NotBlank @Size(max = TAMANHO_DA_DESCRICAO) String descricao, Long categoriaId,
            @Size(max = TAMANHO_DO_DOCUMENTO) String documento, @NotNull @Positive BigDecimal valor,
            @NotNull LocalDate vencimento, @NotNull Long versao) {
    }

    /** @param valorPago o que saiu ou entrou na conta, já com juros e multa e sem o desconto */
    record Baixa(@NotNull Long contaId, @NotNull FormaDePagamento forma, @NotNull @Positive BigDecimal valorPago,
            @PositiveOrZero BigDecimal juros, @PositiveOrZero BigDecimal multa, @PositiveOrZero BigDecimal desconto) {

        static Dinheiro opcional(BigDecimal valor) {
            return valor == null ? null : Dinheiro.de(valor);
        }
    }

    record Motivo(@NotBlank @Size(max = TAMANHO_DO_MOTIVO) String motivo) {
    }
}
