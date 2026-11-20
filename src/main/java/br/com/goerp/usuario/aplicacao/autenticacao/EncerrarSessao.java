package br.com.goerp.usuario.aplicacao.autenticacao;

import java.time.Clock;
import java.time.Instant;

import br.com.goerp.compartilhado.aplicacao.CasoDeUso;
import br.com.goerp.usuario.dominio.EventoDeSeguranca;
import br.com.goerp.usuario.dominio.EventoDeSegurancaRepositorio;
import br.com.goerp.usuario.dominio.SessaoDeAcessoRepositorio;
import br.com.goerp.usuario.dominio.TipoDeEventoDeSeguranca;

/** Logout: revoga o token de renovação. Repetir a operação não tem efeito. */
@CasoDeUso
public class EncerrarSessao {

    private final SessaoDeAcessoRepositorio sessoes;
    private final EventoDeSegurancaRepositorio eventos;
    private final Clock relogio;

    public EncerrarSessao(SessaoDeAcessoRepositorio sessoes, EventoDeSegurancaRepositorio eventos, Clock relogio) {
        this.sessoes = sessoes;
        this.eventos = eventos;
        this.relogio = relogio;
    }

    public void executar(String tokenDeRenovacao) {
        Instant agora = relogio.instant();
        sessoes.buscarPorHashDoToken(TokenDeRenovacao.hash(tokenDeRenovacao))
                .filter(sessao -> !sessao.revogada())
                .ifPresent(sessao -> {
                    sessao.revogar(agora);
                    sessoes.salvar(sessao);
                    eventos.registrar(new EventoDeSeguranca(TipoDeEventoDeSeguranca.LOGOUT, null,
                            sessao.usuarioId(), sessao.empresaId(), agora));
                });
    }
}
