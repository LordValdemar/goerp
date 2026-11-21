package br.com.goerp.compartilhado.infraestrutura.config;

import java.time.Clock;
import java.time.Duration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Relógio único da aplicação. Injetar {@link Clock} em vez de chamar {@code now()} direto
 * permite controlar o tempo nos testes (ex.: expiração de bloqueio e de tokens).
 *
 * <p>O relógio avança em microssegundos, a mesma precisão do PostgreSQL: assim um horário calculado
 * em memória é igual ao que volta do banco (o banco arredondaria os nanossegundos).
 */
@Configuration
public class RelogioConfig {

    private static final Duration PRECISAO_DO_BANCO = Duration.ofNanos(1_000);

    @Bean
    Clock relogio() {
        return Clock.tick(Clock.systemUTC(), PRECISAO_DO_BANCO);
    }
}
