package br.com.goerp.financeiro.aplicacao;

import br.com.goerp.financeiro.dominio.NaturezaDoTitulo;
import br.com.goerp.financeiro.dominio.Titulo;

/** Texto do lançamento gerado por um pagamento, para o extrato dizer do que se trata. */
final class HistoricoDaBaixa {

    private HistoricoDaBaixa() {
    }

    static String de(Titulo titulo) {
        String acao = titulo.natureza() == NaturezaDoTitulo.A_PAGAR ? "Pagamento" : "Recebimento";
        String parcela = titulo.parcelas() > 1 ? " (parcela %d/%d)".formatted(titulo.parcela(), titulo.parcelas()) : "";
        return acao + ": " + titulo.descricao() + parcela;
    }
}
