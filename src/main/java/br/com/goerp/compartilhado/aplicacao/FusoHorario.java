package br.com.goerp.compartilhado.aplicacao;

import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Objects;

/** Fuso da empresa, para regras que falam em "dia" (validade de orçamento, vendas do dia...). */
public record FusoHorario(ZoneId zona) {

    public FusoHorario {
        Objects.requireNonNull(zona, "zona");
    }

    public LocalDate hoje(Clock relogio) {
        return LocalDate.ofInstant(relogio.instant(), zona);
    }
}
