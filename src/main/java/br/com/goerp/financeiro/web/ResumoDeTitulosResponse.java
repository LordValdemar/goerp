package br.com.goerp.financeiro.web;

import java.math.BigDecimal;

import br.com.goerp.financeiro.aplicacao.ResumoDeTitulos;
import br.com.goerp.financeiro.dominio.TotalDeTitulos;

public record ResumoDeTitulosResponse(Total emAberto, Total vencidos, Total vencemHoje) {

    public record Total(long quantidade, BigDecimal valor) {

        static Total de(TotalDeTitulos total) {
            return new Total(total.quantidade(), total.saldoDevedor().valor());
        }
    }

    static ResumoDeTitulosResponse de(ResumoDeTitulos resumo) {
        return new ResumoDeTitulosResponse(Total.de(resumo.emAberto()), Total.de(resumo.vencidos()),
                Total.de(resumo.vencemHoje()));
    }
}
