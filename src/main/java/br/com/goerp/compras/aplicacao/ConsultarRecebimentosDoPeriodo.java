package br.com.goerp.compras.aplicacao;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import br.com.goerp.compartilhado.aplicacao.CasoDeUso;
import br.com.goerp.compartilhado.aplicacao.FusoHorario;
import br.com.goerp.compartilhado.dominio.Dinheiro;
import br.com.goerp.compartilhado.dominio.excecao.CampoObrigatorioException;
import br.com.goerp.compartilhado.dominio.excecao.ValorInvalidoException;
import br.com.goerp.compras.aplicacao.RecebimentosDoPeriodo.Linha;

/** Recebimentos por período e fornecedor, para conferir notas e preparar o contas a pagar. */
@CasoDeUso
public class ConsultarRecebimentosDoPeriodo {

    /** Limite para a consulta não varrer anos de dados de uma vez. */
    static final long DIAS_MAXIMOS = 366;

    private final ConsultasDeCompras consultas;
    private final FusoHorario fuso;

    public ConsultarRecebimentosDoPeriodo(ConsultasDeCompras consultas, FusoHorario fuso) {
        this.consultas = consultas;
        this.fuso = fuso;
    }

    /** @param ate inclusive */
    public RecebimentosDoPeriodo executar(LocalDate de, LocalDate ate, Long fornecedorId, boolean incluirEstornados) {
        validarPeriodo(de, ate);
        List<Linha> linhas = consultas.recebimentos(de.atStartOfDay(fuso.zona()).toInstant(),
                ate.plusDays(1).atStartOfDay(fuso.zona()).toInstant(), fornecedorId, incluirEstornados);

        Map<Long, Dinheiro> porFornecedor = new LinkedHashMap<>();
        Dinheiro total = Dinheiro.ZERO;
        for (Linha linha : linhas.stream().filter(l -> !l.recebimento().estornado()).toList()) {
            Dinheiro valor = linha.recebimento().valorTotal();
            total = total.somar(valor);
            porFornecedor.merge(linha.fornecedorId(), valor, Dinheiro::somar);
        }
        return new RecebimentosDoPeriodo(linhas, total, porFornecedor);
    }

    private static void validarPeriodo(LocalDate de, LocalDate ate) {
        if (de == null || ate == null) {
            throw new CampoObrigatorioException(de == null ? "de" : "ate");
        }
        if (ate.isBefore(de)) {
            throw new ValorInvalidoException("O fim do período não pode ser antes do início");
        }
        if (ChronoUnit.DAYS.between(de, ate) >= DIAS_MAXIMOS) {
            throw new ValorInvalidoException("O período pode ter no máximo " + DIAS_MAXIMOS + " dias");
        }
    }
}
