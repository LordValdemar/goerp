package br.com.goerp.usuario.aplicacao.autenticacao;

import static br.com.goerp.usuario.dominio.TipoDeEventoDeSeguranca.ACESSO_RENOVADO;
import static br.com.goerp.usuario.dominio.TipoDeEventoDeSeguranca.RENOVACAO_RECUSADA;
import static br.com.goerp.usuario.dominio.TipoDeEventoDeSeguranca.TOKEN_REUTILIZADO;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import br.com.goerp.compartilhado.aplicacao.AcessoEntreEmpresas;
import br.com.goerp.compartilhado.aplicacao.CasoDeUso;
import br.com.goerp.empresa.aplicacao.ListarEmpresasAtivas;
import br.com.goerp.empresa.dominio.Empresa;
import br.com.goerp.usuario.aplicacao.autenticacao.ResultadoDaAutenticacao.Autenticado;
import br.com.goerp.usuario.aplicacao.autenticacao.ResultadoDaAutenticacao.Recusado;
import br.com.goerp.usuario.dominio.EventoDeSeguranca;
import br.com.goerp.usuario.dominio.EventoDeSegurancaRepositorio;
import br.com.goerp.usuario.dominio.SessaoDeAcesso;
import br.com.goerp.usuario.dominio.SessaoDeAcessoRepositorio;
import br.com.goerp.usuario.dominio.TipoDeEventoDeSeguranca;
import br.com.goerp.usuario.dominio.Usuario;
import br.com.goerp.usuario.dominio.UsuarioRepositorio;
import br.com.goerp.usuario.dominio.VinculoEmpresa;
import br.com.goerp.usuario.dominio.VinculoEmpresaRepositorio;

/**
 * Troca um token de renovação válido por um novo par de tokens (rotação): o token usado é revogado.
 * Se um token já revogado for apresentado de novo, ele pode ter sido roubado; por segurança,
 * todas as sessões do usuário são encerradas.
 *
 * <p>Acessa todas as empresas porque roda sem usuário autenticado (o token de acesso já expirou).
 */
@CasoDeUso
@AcessoEntreEmpresas
public class RenovarAcesso {

    private static final Recusado SESSAO_INVALIDA = new Recusado(MotivoDaRecusa.SESSAO_INVALIDA);

    private final SessaoDeAcessoRepositorio sessoes;
    private final UsuarioRepositorio usuarios;
    private final VinculoEmpresaRepositorio vinculos;
    private final ListarEmpresasAtivas listarEmpresasAtivas;
    private final EmissorDeTokens emissorDeTokens;
    private final EventoDeSegurancaRepositorio eventos;
    private final Clock relogio;

    public RenovarAcesso(SessaoDeAcessoRepositorio sessoes, UsuarioRepositorio usuarios,
            VinculoEmpresaRepositorio vinculos, ListarEmpresasAtivas listarEmpresasAtivas,
            EmissorDeTokens emissorDeTokens, EventoDeSegurancaRepositorio eventos, Clock relogio) {
        this.sessoes = sessoes;
        this.usuarios = usuarios;
        this.vinculos = vinculos;
        this.listarEmpresasAtivas = listarEmpresasAtivas;
        this.emissorDeTokens = emissorDeTokens;
        this.eventos = eventos;
        this.relogio = relogio;
    }

    public ResultadoDaAutenticacao executar(String tokenDeRenovacao) {
        Instant agora = relogio.instant();
        Optional<SessaoDeAcesso> encontrada = sessoes.buscarPorHashDoToken(TokenDeRenovacao.hash(tokenDeRenovacao));
        if (encontrada.isEmpty()) {
            return SESSAO_INVALIDA;
        }
        SessaoDeAcesso sessao = encontrada.get();
        Usuario usuario = usuarios.buscarPorId(sessao.usuarioId()).orElseThrow();

        if (sessao.revogada()) {
            sessoes.revogarTodasDoUsuario(usuario.id(), agora);
            registrar(TOKEN_REUTILIZADO, usuario, sessao, agora);
            return SESSAO_INVALIDA;
        }
        boolean dentroDaValidade = sessao.validaEm(agora);
        sessao.revogar(agora);
        sessoes.salvar(sessao);

        Optional<Empresa> empresa = empresaDaSessao(sessao);
        boolean podeContinuar = dentroDaValidade
                && usuario.ativo()
                && !usuario.estaBloqueado(agora)
                && (sessao.empresaId() == null || empresa.isPresent());
        if (!podeContinuar) {
            registrar(RENOVACAO_RECUSADA, usuario, sessao, agora);
            return SESSAO_INVALIDA;
        }

        TokensEmitidos tokens = emissorDeTokens.emitir(usuario, empresa.orElse(null));
        registrar(ACESSO_RENOVADO, usuario, sessao, agora);
        return new Autenticado(tokens, usuario, empresa.orElse(null));
    }

    /** A empresa da sessão, se o vínculo do usuário e a empresa continuam ativos. */
    private Optional<Empresa> empresaDaSessao(SessaoDeAcesso sessao) {
        if (sessao.empresaId() == null) {
            return Optional.empty();
        }
        boolean vinculoAtivo = vinculos.buscar(sessao.usuarioId(), sessao.empresaId())
                .map(VinculoEmpresa::ativo)
                .orElse(false);
        if (!vinculoAtivo) {
            return Optional.empty();
        }
        return listarEmpresasAtivas.executar(List.of(sessao.empresaId())).stream().findFirst();
    }

    private void registrar(TipoDeEventoDeSeguranca tipo, Usuario usuario, SessaoDeAcesso sessao, Instant agora) {
        eventos.registrar(EventoDeSeguranca.doUsuario(tipo, usuario, sessao.empresaId(), agora));
    }
}
