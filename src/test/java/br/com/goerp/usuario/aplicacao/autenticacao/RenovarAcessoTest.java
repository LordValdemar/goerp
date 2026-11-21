package br.com.goerp.usuario.aplicacao.autenticacao;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import br.com.goerp.compartilhado.dominio.Cnpj;
import br.com.goerp.compartilhado.dominio.Email;
import br.com.goerp.empresa.aplicacao.ListarEmpresasAtivas;
import br.com.goerp.empresa.dominio.Empresa;
import br.com.goerp.usuario.aplicacao.autenticacao.ResultadoDaAutenticacao.Autenticado;
import br.com.goerp.usuario.aplicacao.autenticacao.ResultadoDaAutenticacao.Recusado;
import br.com.goerp.usuario.dominio.ControleDeAcesso;
import br.com.goerp.usuario.dominio.EventoDeSegurancaRepositorio;
import br.com.goerp.usuario.dominio.SenhaCriptografada;
import br.com.goerp.usuario.dominio.SessaoDeAcesso;
import br.com.goerp.usuario.dominio.SessaoDeAcessoRepositorio;
import br.com.goerp.usuario.dominio.Usuario;
import br.com.goerp.usuario.dominio.UsuarioRepositorio;
import br.com.goerp.usuario.dominio.VinculoEmpresa;
import br.com.goerp.usuario.dominio.VinculoEmpresaRepositorio;

class RenovarAcessoTest {

    private static final Instant AGORA = Instant.parse("2026-11-20T12:00:00Z");
    private static final String TOKEN = "token-de-renovacao";
    private static final Recusado SESSAO_INVALIDA = new Recusado(MotivoDaRecusa.SESSAO_INVALIDA);
    private static final Empresa EMPRESA = Empresa.reconstituir(10L, Cnpj.de("11222333000181"), "A LTDA", null, true);
    private static final TokensEmitidos TOKENS = new TokensEmitidos(new TokenDeAcesso("jwt", AGORA), "novo", AGORA);

    SessaoDeAcessoRepositorio sessoes = mock(SessaoDeAcessoRepositorio.class);
    UsuarioRepositorio usuarios = mock(UsuarioRepositorio.class);
    VinculoEmpresaRepositorio vinculos = mock(VinculoEmpresaRepositorio.class);
    ListarEmpresasAtivas listarEmpresasAtivas = mock(ListarEmpresasAtivas.class);
    EmissorDeTokens emissor = mock(EmissorDeTokens.class);
    RenovarAcesso renovar;
    Usuario maria;

    @BeforeEach
    void preparar() {
        renovar = new RenovarAcesso(sessoes, usuarios, vinculos, listarEmpresasAtivas, emissor,
                mock(EventoDeSegurancaRepositorio.class), Clock.fixed(AGORA, ZoneOffset.UTC));
        maria = Usuario.reconstituir(1L, "Maria", Email.de("maria@exemplo.com"), new SenhaCriptografada("h"), false,
                true, ControleDeAcesso.livre());
        when(usuarios.buscarPorId(1L)).thenReturn(Optional.of(maria));
        when(vinculos.buscar(1L, 10L)).thenReturn(Optional.of(VinculoEmpresa.reconstituir(5L, 1L, 10L, 1L, true)));
        when(listarEmpresasAtivas.executar(List.of(10L))).thenReturn(List.of(EMPRESA));
        when(emissor.emitir(any(), any())).thenReturn(TOKENS);
    }

    @Test
    void trocaPorNovosTokensERevogaOAntigo() {
        SessaoDeAcesso sessao = sessao(AGORA.minus(Duration.ofDays(1)));

        var resultado = renovar.executar(TOKEN);

        assertThat(resultado).isEqualTo(new Autenticado(TOKENS, maria, EMPRESA));
        assertThat(sessao.revogada()).isTrue();
    }

    @Test
    void tokenDesconhecidoEhRecusado() {
        assertThat(renovar.executar("inexistente")).isEqualTo(SESSAO_INVALIDA);
    }

    @Test
    void sessaoExpiradaEhRecusada() {
        sessao(AGORA.minus(Duration.ofDays(8)));

        assertThat(renovar.executar(TOKEN)).isEqualTo(SESSAO_INVALIDA);
        verify(emissor, never()).emitir(any(), any());
    }

    @Test
    void tokenReutilizadoEncerraTodasAsSessoesDoUsuario() {
        SessaoDeAcesso sessao = sessao(AGORA.minus(Duration.ofDays(1)));
        sessao.revogar(AGORA);

        assertThat(renovar.executar(TOKEN)).isEqualTo(SESSAO_INVALIDA);
        verify(sessoes).revogarTodasDoUsuario(1L, AGORA);
    }

    @Test
    void vinculoInativadoImpedeARenovacao() {
        sessao(AGORA.minus(Duration.ofDays(1)));
        when(vinculos.buscar(1L, 10L)).thenReturn(Optional.of(VinculoEmpresa.reconstituir(5L, 1L, 10L, 1L, false)));

        assertThat(renovar.executar(TOKEN)).isEqualTo(SESSAO_INVALIDA);
    }

    @Test
    void usuarioInativadoNaoRenova() {
        sessao(AGORA.minus(Duration.ofDays(1)));
        maria.inativar();

        assertThat(renovar.executar(TOKEN)).isEqualTo(SESSAO_INVALIDA);
    }

    private SessaoDeAcesso sessao(Instant abertaEm) {
        SessaoDeAcesso sessao = SessaoDeAcesso.abrir(1L, 10L, TokenDeRenovacao.hash(TOKEN), abertaEm, Duration.ofDays(7));
        when(sessoes.buscarPorHashDoToken(TokenDeRenovacao.hash(TOKEN))).thenReturn(Optional.of(sessao));
        return sessao;
    }
}
