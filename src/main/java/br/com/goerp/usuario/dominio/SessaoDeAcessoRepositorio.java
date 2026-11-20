package br.com.goerp.usuario.dominio;

import java.time.Instant;
import java.util.Optional;

public interface SessaoDeAcessoRepositorio {

    SessaoDeAcesso salvar(SessaoDeAcesso sessao);

    Optional<SessaoDeAcesso> buscarPorHashDoToken(String hashDoToken);

    void revogarTodasDoUsuario(Long usuarioId, Instant agora);
}
