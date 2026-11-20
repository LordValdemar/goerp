package br.com.goerp.compartilhado.infraestrutura.seguranca;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;

/**
 * API sem estado: cada requisição traz o token de acesso. Só login, renovação, logout e health
 * são públicos; o resto exige autenticação e, nos endpoints de negócio, uma permissão ({@code @PreAuthorize}).
 */
@Configuration
@EnableMethodSecurity
public class SegurancaWebConfig {

    private static final String[] ROTAS_PUBLICAS = {
        "/api/v1/autenticacao/login",
        "/api/v1/autenticacao/renovar",
        "/api/v1/autenticacao/sair",
    };

    @Bean
    SecurityFilterChain filtrosDeSeguranca(HttpSecurity http, RespostasDeErroDeSeguranca respostasDeErro)
            throws Exception {
        return http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(sessao -> sessao.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(rotas -> rotas
                        .requestMatchers(HttpMethod.POST, ROTAS_PUBLICAS).permitAll()
                        .requestMatchers(HttpMethod.GET, "/actuator/health").permitAll()
                        .anyRequest().authenticated())
                .oauth2ResourceServer(servidor -> servidor
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(conversorDePermissoes()))
                        .authenticationEntryPoint(respostasDeErro))
                .exceptionHandling(erros -> erros
                        .authenticationEntryPoint(respostasDeErro)
                        .accessDeniedHandler(respostasDeErro))
                .build();
    }

    /** As permissões do token viram authorities sem prefixo: {@code hasAuthority('USUARIO_GERENCIAR')}. */
    private static JwtAuthenticationConverter conversorDePermissoes() {
        var permissoes = new JwtGrantedAuthoritiesConverter();
        permissoes.setAuthoritiesClaimName(UsuarioAutenticado.CLAIM_PERMISSOES);
        permissoes.setAuthorityPrefix("");
        var conversor = new JwtAuthenticationConverter();
        conversor.setJwtGrantedAuthoritiesConverter(permissoes);
        return conversor;
    }
}
