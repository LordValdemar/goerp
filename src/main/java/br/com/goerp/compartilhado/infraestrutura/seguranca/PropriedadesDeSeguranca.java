package br.com.goerp.compartilhado.infraestrutura.seguranca;

import java.nio.charset.StandardCharsets;
import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuração de segurança ({@code goerp.seguranca.*}).
 *
 * @param segredoJwt chave HMAC para assinar os tokens; vem da variável de ambiente JWT_SECRET
 */
@ConfigurationProperties(prefix = "goerp.seguranca")
public record PropriedadesDeSeguranca(
        String segredoJwt,
        Duration validadeDoAcesso,
        Duration validadeDaRenovacao) {

    /** HS256 exige chave de pelo menos 256 bits. */
    private static final int TAMANHO_MINIMO_DO_SEGREDO = 32;

    public PropriedadesDeSeguranca {
        if (segredoJwt == null || segredoJwt.getBytes(StandardCharsets.UTF_8).length < TAMANHO_MINIMO_DO_SEGREDO) {
            throw new IllegalStateException("Defina JWT_SECRET com pelo menos "
                    + TAMANHO_MINIMO_DO_SEGREDO + " caracteres");
        }
    }
}
