package br.com.goerp.usuario.aplicacao.autenticacao;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.HexFormat;

/**
 * Token de renovação: valor aleatório e imprevisível entregue ao cliente.
 * No banco fica apenas o hash SHA-256, como se faz com senhas.
 */
public final class TokenDeRenovacao {

    private static final int TAMANHO_EM_BYTES = 32;
    private static final SecureRandom ALEATORIO = new SecureRandom();

    private TokenDeRenovacao() {
    }

    public static String gerar() {
        byte[] bytes = new byte[TAMANHO_EM_BYTES];
        ALEATORIO.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    public static String hash(String token) {
        try {
            byte[] resumo = MessageDigest.getInstance("SHA-256").digest(token.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(resumo);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 indisponível na JVM", e);
        }
    }
}
