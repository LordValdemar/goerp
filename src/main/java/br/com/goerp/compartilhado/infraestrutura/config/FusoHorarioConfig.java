package br.com.goerp.compartilhado.infraestrutura.config;

import java.time.ZoneId;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import br.com.goerp.compartilhado.aplicacao.FusoHorario;

@Configuration
public class FusoHorarioConfig {

    @Bean
    FusoHorario fusoHorario(@Value("${goerp.fuso-horario}") String zona) {
        return new FusoHorario(ZoneId.of(zona));
    }
}
