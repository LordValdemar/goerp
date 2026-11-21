package br.com.goerp.compras.aplicacao;

import java.time.LocalDate;
import java.util.List;

import br.com.goerp.compartilhado.dominio.Dinheiro;
import br.com.goerp.financeiro.dominio.FormaDePagamento;
import br.com.goerp.financeiro.dominio.Parcelamento;

/**
 * Como a nota será paga ao fornecedor: datas em {@code vencimentos}, ou {@code parcelas} mensais
 * (padrão 1) a partir do {@code primeiroVencimento} (padrão: o dia do recebimento).
 *
 * @param formaPrevista ex.: BOLETO (padrão A_PRAZO)
 * @param categoriaId   categoria de despesa (opcional)
 */
public record CondicaoDePagamento(Integer parcelas, LocalDate primeiroVencimento, List<LocalDate> vencimentos,
        FormaDePagamento formaPrevista, Long categoriaId) {

    /** Sem condição informada: à vista, vencendo no dia. */
    public static CondicaoDePagamento noDia() {
        return new CondicaoDePagamento(null, null, null, null, null);
    }

    Parcelamento parcelamento(Dinheiro total, LocalDate hoje) {
        if (vencimentos != null && !vencimentos.isEmpty()) {
            return Parcelamento.comVencimentos(total, vencimentos);
        }
        return Parcelamento.mensal(total, parcelas == null ? 1 : parcelas,
                primeiroVencimento == null ? hoje : primeiroVencimento);
    }
}
