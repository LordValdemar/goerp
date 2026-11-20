package br.com.goerp.usuario.dominio;

import java.time.Duration;
import java.time.Instant;
import java.util.Objects;

/**
 * Sessão aberta por um login. É identificada pelo token de renovação, do qual só o hash
 * é guardado: quem lê o banco não consegue usar o token.
 */
public class SessaoDeAcesso {

    private final Long id;
    private final Long usuarioId;
    private final Long empresaId;
    private final String hashDoToken;
    private final Instant expiraEm;
    private Instant revogadaEm;

    private SessaoDeAcesso(Long id, Long usuarioId, Long empresaId, String hashDoToken, Instant expiraEm,
            Instant revogadaEm) {
        this.id = id;
        this.usuarioId = Objects.requireNonNull(usuarioId, "usuarioId");
        this.empresaId = empresaId;
        this.hashDoToken = Objects.requireNonNull(hashDoToken, "hashDoToken");
        this.expiraEm = Objects.requireNonNull(expiraEm, "expiraEm");
        this.revogadaEm = revogadaEm;
    }

    /** @param empresaId nulo quando a sessão é do administrador da plataforma, fora de uma empresa */
    public static SessaoDeAcesso abrir(Long usuarioId, Long empresaId, String hashDoToken, Instant agora,
            Duration validade) {
        return new SessaoDeAcesso(null, usuarioId, empresaId, hashDoToken, agora.plus(validade), null);
    }

    public static SessaoDeAcesso reconstituir(Long id, Long usuarioId, Long empresaId, String hashDoToken,
            Instant expiraEm, Instant revogadaEm) {
        return new SessaoDeAcesso(id, usuarioId, empresaId, hashDoToken, expiraEm, revogadaEm);
    }

    public boolean validaEm(Instant agora) {
        return !revogada() && agora.isBefore(expiraEm);
    }

    public boolean revogada() {
        return revogadaEm != null;
    }

    public void revogar(Instant agora) {
        if (!revogada()) {
            revogadaEm = agora;
        }
    }

    public Long id() {
        return id;
    }

    public Long usuarioId() {
        return usuarioId;
    }

    public Long empresaId() {
        return empresaId;
    }

    public String hashDoToken() {
        return hashDoToken;
    }

    public Instant expiraEm() {
        return expiraEm;
    }

    public Instant revogadaEm() {
        return revogadaEm;
    }
}
