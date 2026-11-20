package br.com.goerp.compartilhado.infraestrutura.auditoria;

import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.Optional;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.auditing.DateTimeProvider;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import br.com.goerp.compartilhado.infraestrutura.seguranca.UsuarioAutenticado;

@Configuration
@EnableJpaAuditing(auditorAwareRef = "auditor", dateTimeProviderRef = "dataHoraDaAuditoria")
public class AuditoriaConfig {

    @Bean
    AuditorAware<Long> auditor() {
        return UsuarioAutenticado::idUsuario;
    }

    @Bean
    DateTimeProvider dataHoraDaAuditoria(Clock relogio) {
        return () -> Optional.of(OffsetDateTime.now(relogio));
    }
}
