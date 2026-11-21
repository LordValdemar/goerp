package br.com.goerp.financeiro.dominio;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.IntStream;

import br.com.goerp.compartilhado.dominio.Dinheiro;

/**
 * Divide um valor em parcelas que somam exatamente o total. A divisão é para baixo, em centavos,
 * e a sobra vai para a primeira parcela (ex.: 100,00 em 3 = 33,34 + 33,33 + 33,33).
 */
public record Parcelamento(List<Parcela> parcelas) {

    /** Limite para evitar parcelamentos absurdos por erro de digitação (5 anos de parcelas mensais). */
    public static final int MAXIMO_DE_PARCELAS = 60;

    public Parcelamento {
        parcelas = List.copyOf(parcelas);
    }

    public static Parcelamento aVista(Dinheiro total, LocalDate vencimento) {
        return comVencimentos(total, List.of(vencimento));
    }

    /** Parcelas mensais a partir do primeiro vencimento (no mês sem o dia, vale o último dia). */
    public static Parcelamento mensal(Dinheiro total, int quantidade, LocalDate primeiroVencimento) {
        Objects.requireNonNull(primeiroVencimento, "primeiroVencimento");
        exigirQuantidade(quantidade);
        return comVencimentos(total, IntStream.range(0, quantidade).mapToObj(primeiroVencimento::plusMonths).toList());
    }

    public static Parcelamento comVencimentos(Dinheiro total, List<LocalDate> vencimentos) {
        Objects.requireNonNull(total, "total");
        exigirQuantidade(vencimentos.size());
        exigirEmOrdem(vencimentos);
        if (total.compareTo(Dinheiro.ZERO) <= 0) {
            throw new ParcelamentoInvalidoException("O valor a parcelar precisa ser maior que zero");
        }
        int quantidade = vencimentos.size();
        BigDecimal base = total.valor().divide(BigDecimal.valueOf(quantidade), Dinheiro.CASAS_DECIMAIS, RoundingMode.DOWN);
        if (base.signum() == 0) {
            throw new ParcelamentoInvalidoException("O valor " + total + " não dá " + quantidade + " parcelas de pelo menos 0,01");
        }
        Dinheiro sobra = total.subtrair(Dinheiro.de(base.multiply(BigDecimal.valueOf(quantidade))));
        List<Parcela> parcelas = new ArrayList<>();
        for (int i = 0; i < quantidade; i++) {
            Dinheiro valor = i == 0 ? Dinheiro.de(base).somar(sobra) : Dinheiro.de(base);
            parcelas.add(new Parcela(i + 1, quantidade, valor, vencimentos.get(i)));
        }
        return new Parcelamento(parcelas);
    }

    public Dinheiro total() {
        return parcelas.stream().map(Parcela::valor).reduce(Dinheiro.ZERO, Dinheiro::somar);
    }

    private static void exigirQuantidade(int quantidade) {
        if (quantidade < 1 || quantidade > MAXIMO_DE_PARCELAS) {
            throw new ParcelamentoInvalidoException("O parcelamento deve ter de 1 a " + MAXIMO_DE_PARCELAS + " parcelas");
        }
    }

    private static void exigirEmOrdem(List<LocalDate> vencimentos) {
        for (int i = 0; i < vencimentos.size(); i++) {
            Objects.requireNonNull(vencimentos.get(i), "vencimento");
            if (i > 0 && !vencimentos.get(i).isAfter(vencimentos.get(i - 1))) {
                throw new ParcelamentoInvalidoException("Os vencimentos devem estar em ordem, sem datas repetidas");
            }
        }
    }
}
