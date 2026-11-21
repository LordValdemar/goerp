package br.com.goerp.compartilhado.infraestrutura.seguranca;

import java.nio.charset.StandardCharsets;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

import com.nimbusds.jose.jwk.source.ImmutableSecret;

/** Assinatura e validação dos tokens de acesso com HMAC-SHA256. */
@Configuration
@EnableConfigurationProperties(PropriedadesDeSeguranca.class)
public class JwtConfig {

    public static final MacAlgorithm ALGORITMO = MacAlgorithm.HS256;

    @Bean
    SecretKey chaveJwt(PropriedadesDeSeguranca propriedades) {
        return new SecretKeySpec(propriedades.segredoJwt().getBytes(StandardCharsets.UTF_8), "HmacSHA256");
    }

    @Bean
    JwtEncoder codificadorJwt(SecretKey chaveJwt) {
        return new NimbusJwtEncoder(new ImmutableSecret<>(chaveJwt));
    }

    @Bean
    JwtDecoder decodificadorJwt(SecretKey chaveJwt) {
        return NimbusJwtDecoder.withSecretKey(chaveJwt).macAlgorithm(ALGORITMO).build();
    }
}
