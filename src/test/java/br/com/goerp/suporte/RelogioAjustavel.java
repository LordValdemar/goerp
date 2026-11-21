package br.com.goerp.suporte;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;

/** Relógio controlado pelo teste: permite avançar o tempo (ex.: fim do bloqueio, expiração de sessão). */
public class RelogioAjustavel extends Clock {

    private Instant agora = agoraNaPrecisaoDoBanco();

    public void avancar(Duration duracao) {
        agora = agora.plus(duracao);
    }

    public void voltarParaAgora() {
        agora = agoraNaPrecisaoDoBanco();
    }

    private static Instant agoraNaPrecisaoDoBanco() {
        return Instant.now().truncatedTo(ChronoUnit.MICROS);
    }

    @Override
    public Instant instant() {
        return agora;
    }

    @Override
    public ZoneId getZone() {
        return ZoneOffset.UTC;
    }

    @Override
    public Clock withZone(ZoneId zona) {
        return this;
    }
}
