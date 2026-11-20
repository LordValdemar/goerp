package br.com.goerp.compartilhado.infraestrutura.config;

import java.time.Clock;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Relógio único da aplicação. Injetar {@link Clock} em vez de chamar {@code now()} direto
 * permite controlar o tempo nos testes (ex.: expiração de bloqueio e de tokens).
 */
@Configuration
public class RelogioConfig {

    @Bean
    Clock relogio() {
        return Clock.systemUTC();
    }
}
