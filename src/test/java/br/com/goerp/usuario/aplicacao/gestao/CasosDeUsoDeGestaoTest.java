package br.com.goerp.usuario.aplicacao.gestao;

import static br.com.goerp.usuario.dominio.Permissao.USUARIO_LER;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import br.com.goerp.compartilhado.dominio.Email;
import br.com.goerp.compartilhado.dominio.excecao.CampoObrigatorioException;
import br.com.goerp.compartilhado.dominio.excecao.RecursoNaoEncontradoException;
import br.com.goerp.usuario.aplicacao.CodificadorDeSenha;
import br.com.goerp.usuario.aplicacao.gestao.TrocarPropriaSenha.ResultadoDaTrocaDeSenha;
import br.com.goerp.usuario.dominio.AutoInativacaoException;
import br.com.goerp.usuario.dominio.ControleDeAcesso;
import br.com.goerp.usuario.dominio.EventoDeSegurancaRepositorio;
import br.com.goerp.usuario.dominio.Perfil;
import br.com.goerp.usuario.dominio.PerfilInativoException;
import br.com.goerp.usuario.dominio.PerfilRepositorio;
import br.com.goerp.usuario.dominio.SenhaCriptografada;
import br.com.goerp.usuario.dominio.SenhaRepetidaException;
import br.com.goerp.usuario.dominio.SessaoDeAcessoRepositorio;
import br.com.goerp.usuario.dominio.UltimoAdministradorException;
import br.com.goerp.usuario.dominio.Usuario;
import br.com.goerp.usuario.dominio.UsuarioJaVinculadoException;
import br.com.goerp.usuario.dominio.UsuarioRepositorio;
import br.com.goerp.usuario.dominio.VinculoEmpresa;
import br.com.goerp.usuario.dominio.VinculoEmpresaRepositorio;

class CasosDeUsoDeGestaoTest {

    private static final Long EMPRESA = 7L;
    private static final Long EU = 1L;
    private static final Long MARIA = 2L;
    private static final Instant AGORA = Instant.parse("2026-11-20T12:00:00Z");
    private static final SenhaCriptografada HASH = new SenhaCriptografada("hash");

    UsuarioRepositorio usuarios = mock(UsuarioRepositorio.class);
    VinculoEmpresaRepositorio vinculos = mock(VinculoEmpresaRepositorio.class);
    PerfilRepositorio perfis = mock(PerfilRepositorio.class);
    SessaoDeAcessoRepositorio sessoes = mock(SessaoDeAcessoRepositorio.class);
    CodificadorDeSenha codificador = mock(CodificadorDeSenha.class);
    Clock relogio = Clock.fixed(AGORA, ZoneOffset.UTC);
    UsuariosDaEmpresa usuariosDaEmpresa = new UsuariosDaEmpresa(usuarios, vinculos, perfis, () -> EMPRESA);

    Perfil administrador = Perfil.reconstituir(10L, EMPRESA, "Administrador", Set.of(USUARIO_LER), true, true);
    Perfil caixa = Perfil.reconstituir(11L, EMPRESA, "Caixa", Set.of(USUARIO_LER), false, true);
    Usuario maria = Usuario.reconstituir(MARIA, "Maria", Email.de("maria@exemplo.com"), HASH, false, true,
            ControleDeAcesso.livre());

    @BeforeEach
    void preparar() {
        when(perfis.buscarPorId(10L)).thenReturn(Optional.of(administrador));
        when(perfis.buscarPorId(11L)).thenReturn(Optional.of(caixa));
        when(usuarios.buscarPorId(MARIA)).thenReturn(Optional.of(maria));
        when(usuarios.salvar(any())).thenAnswer(chamada -> chamada.getArgument(0));
        when(vinculos.salvar(any())).thenAnswer(chamada -> chamada.getArgument(0));
        when(codificador.criptografar(any())).thenReturn(HASH);
    }

    // ---------- cadastro ----------

    @Test
    void cadastraUsuarioNovoComSenhaCriptografada() {
        Usuario salvo = Usuario.reconstituir(3L, "Novo", Email.de("novo@exemplo.com"), HASH, false, true,
                ControleDeAcesso.livre());
        when(usuarios.salvar(any())).thenReturn(salvo);

        var resultado = cadastrar("novo@exemplo.com", "segredo123", 11L);

        assertThat(resultado.usuarioJaExistia()).isFalse();
        assertThat(resultado.usuarioDaEmpresa().vinculo().empresaId()).isEqualTo(EMPRESA);
        assertThat(resultado.usuarioDaEmpresa().perfil()).isEqualTo(caixa);
    }

    @Test
    void usuarioNovoPrecisaDeSenha() {
        assertThatThrownBy(() -> cadastrar("novo@exemplo.com", null, 11L))
                .isInstanceOf(CampoObrigatorioException.class);
    }

    @Test
    void usuarioExistenteSoGanhaOVinculoSemMudarASenha() {
        when(usuarios.buscarPorEmail(maria.email())).thenReturn(Optional.of(maria));

        var resultado = cadastrar("maria@exemplo.com", "outraSenha99", 11L);

        assertThat(resultado.usuarioJaExistia()).isTrue();
        verify(usuarios, never()).salvar(any());
        verify(codificador, never()).criptografar(any());
    }

    @Test
    void naoVinculaDuasVezes() {
        when(usuarios.buscarPorEmail(maria.email())).thenReturn(Optional.of(maria));
        when(vinculos.buscar(MARIA, EMPRESA)).thenReturn(Optional.of(VinculoEmpresa.novo(MARIA, EMPRESA, 11L)));

        assertThatThrownBy(() -> cadastrar("maria@exemplo.com", null, 11L))
                .isInstanceOf(UsuarioJaVinculadoException.class);
    }

    @Test
    void naoCadastraComPerfilInativoOuInexistente() {
        caixa.inativar();

        assertThatThrownBy(() -> cadastrar("novo@exemplo.com", "segredo123", 11L))
                .isInstanceOf(PerfilInativoException.class);
        assertThatThrownBy(() -> cadastrar("novo@exemplo.com", "segredo123", 99L))
                .isInstanceOf(RecursoNaoEncontradoException.class);
    }

    // ---------- inativação e último administrador ----------

    @Test
    void ninguemInativaOProprioAcesso() {
        assertThatThrownBy(() -> inativar(EU)).isInstanceOf(AutoInativacaoException.class);
    }

    @Test
    void inativaAcessoEEncerraAsSessoesDoUsuarioNaEmpresa() {
        VinculoEmpresa vinculo = vincularMaria(11L);

        inativar(MARIA);

        assertThat(vinculo.ativo()).isFalse();
        verify(sessoes).revogarTodasDoUsuarioNaEmpresa(MARIA, EMPRESA, AGORA);
    }

    @Test
    void naoInativaOUltimoAdministrador() {
        vincularMaria(10L);
        when(vinculos.contarAtivosComPerfil(10L)).thenReturn(1L);

        assertThatThrownBy(() -> inativar(MARIA)).isInstanceOf(UltimoAdministradorException.class);
    }

    @Test
    void inativaAdministradorSeHouverOutro() {
        VinculoEmpresa vinculo = vincularMaria(10L);
        when(vinculos.contarAtivosComPerfil(10L)).thenReturn(2L);

        inativar(MARIA);

        assertThat(vinculo.ativo()).isFalse();
    }

    @Test
    void naoTiraOUltimoAdministradorDaFuncao() {
        vincularMaria(10L);
        when(vinculos.contarAtivosComPerfil(10L)).thenReturn(1L);

        assertThatThrownBy(() -> new TrocarPerfilDoUsuario(vinculos, usuariosDaEmpresa).executar(MARIA, 11L))
                .isInstanceOf(UltimoAdministradorException.class);
    }

    @Test
    void trocaPerfil() {
        VinculoEmpresa vinculo = vincularMaria(11L);

        new TrocarPerfilDoUsuario(vinculos, usuariosDaEmpresa).executar(MARIA, 10L);

        assertThat(vinculo.perfilId()).isEqualTo(10L);
    }

    @Test
    void usuarioDeOutraEmpresaNaoEhEncontrado() {
        assertThatThrownBy(() -> inativar(MARIA)).isInstanceOf(RecursoNaoEncontradoException.class);
    }

    // ---------- troca da própria senha ----------

    @Test
    void trocaSenhaEEncerraTodasAsSessoes() {
        when(codificador.confere("segredo123", HASH)).thenReturn(true);
        when(codificador.criptografar(any())).thenReturn(new SenhaCriptografada("novo-hash"));

        var resultado = trocarSenhaDaMaria("segredo123", "novaSenha456");

        assertThat(resultado).isEqualTo(ResultadoDaTrocaDeSenha.SENHA_ALTERADA);
        assertThat(maria.senha().hash()).isEqualTo("novo-hash");
        verify(sessoes).revogarTodasDoUsuario(MARIA, AGORA);
    }

    @Test
    void senhaAtualErradaContaComoTentativaFalha() {
        var resultado = trocarSenhaDaMaria("errada123", "novaSenha456");

        assertThat(resultado).isEqualTo(ResultadoDaTrocaDeSenha.SENHA_ATUAL_INCORRETA);
        assertThat(maria.controleDeAcesso().tentativasFalhas()).isEqualTo(1);
        verify(sessoes, never()).revogarTodasDoUsuario(any(), any());
    }

    @Test
    void novaSenhaIgualAAtualEhRecusada() {
        when(codificador.confere("segredo123", HASH)).thenReturn(true);

        assertThatThrownBy(() -> trocarSenhaDaMaria("segredo123", "segredo123"))
                .isInstanceOf(SenhaRepetidaException.class);
    }

    private ResultadoDaTrocaDeSenha trocarSenhaDaMaria(String atual, String nova) {
        return new TrocarPropriaSenha(usuarios, sessoes, mock(EventoDeSegurancaRepositorio.class), codificador,
                () -> MARIA, relogio).executar(atual, nova);
    }

    private ResultadoDoCadastro cadastrar(String email, String senha, Long perfilId) {
        return new CadastrarUsuarioNaEmpresa(usuarios, vinculos, usuariosDaEmpresa, codificador, () -> EMPRESA)
                .executar(new DadosDoNovoUsuario("Novo", email, senha, perfilId));
    }

    private void inativar(Long usuarioId) {
        new InativarUsuarioNaEmpresa(vinculos, sessoes, usuariosDaEmpresa, () -> EU, () -> EMPRESA, relogio)
                .executar(usuarioId);
    }

    private VinculoEmpresa vincularMaria(Long perfilId) {
        VinculoEmpresa vinculo = VinculoEmpresa.reconstituir(50L, MARIA, EMPRESA, perfilId, true);
        when(vinculos.buscar(MARIA, EMPRESA)).thenReturn(Optional.of(vinculo));
        return vinculo;
    }
}
