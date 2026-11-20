package br.com.goerp.usuario.aplicacao.autenticacao;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
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
import br.com.goerp.usuario.aplicacao.CodificadorDeSenha;
import br.com.goerp.usuario.aplicacao.autenticacao.ResultadoDaAutenticacao.Autenticado;
import br.com.goerp.usuario.aplicacao.autenticacao.ResultadoDaAutenticacao.Recusado;
import br.com.goerp.usuario.aplicacao.autenticacao.ResultadoDaAutenticacao.SelecaoDeEmpresaNecessaria;
import br.com.goerp.usuario.dominio.ControleDeAcesso;
import br.com.goerp.usuario.dominio.EventoDeSegurancaRepositorio;
import br.com.goerp.usuario.dominio.SenhaCriptografada;
import br.com.goerp.usuario.dominio.TipoDeEventoDeSeguranca;
import br.com.goerp.usuario.dominio.Usuario;
import br.com.goerp.usuario.dominio.UsuarioRepositorio;
import br.com.goerp.usuario.dominio.VinculoEmpresa;
import br.com.goerp.usuario.dominio.VinculoEmpresaRepositorio;

class AutenticarUsuarioTest {

    private static final Instant AGORA = Instant.parse("2026-11-20T12:00:00Z");
    private static final String SENHA_CERTA = "segredo123";
    private static final SenhaCriptografada HASH = new SenhaCriptografada("hash-certo");
    private static final Empresa EMPRESA_A = Empresa.reconstituir(10L, Cnpj.de("11222333000181"), "A LTDA", null, true);
    private static final Empresa EMPRESA_B = Empresa.reconstituir(20L, Cnpj.de("11444777000161"), "B LTDA", null, true);
    private static final TokensEmitidos TOKENS =
            new TokensEmitidos(new TokenDeAcesso("jwt", AGORA), "renovacao", AGORA);

    UsuarioRepositorio usuarios = mock(UsuarioRepositorio.class);
    VinculoEmpresaRepositorio vinculos = mock(VinculoEmpresaRepositorio.class);
    ListarEmpresasAtivas listarEmpresasAtivas = mock(ListarEmpresasAtivas.class);
    CodificadorDeSenha codificador = mock(CodificadorDeSenha.class);
    EmissorDeTokens emissor = mock(EmissorDeTokens.class);
    EventoDeSegurancaRepositorio eventos = mock(EventoDeSegurancaRepositorio.class);

    AutenticarUsuario autenticar;
    Usuario maria;

    @BeforeEach
    void preparar() {
        when(codificador.criptografar(any())).thenReturn(new SenhaCriptografada("ficticia"));
        when(codificador.confere(SENHA_CERTA, HASH)).thenReturn(true);
        when(emissor.emitir(any(), any())).thenReturn(TOKENS);
        autenticar = new AutenticarUsuario(usuarios, vinculos, listarEmpresasAtivas, codificador, emissor, eventos,
                Clock.fixed(AGORA, ZoneOffset.UTC));
        maria = Usuario.reconstituir(1L, "Maria", Email.de("maria@exemplo.com"), HASH, false, true,
                ControleDeAcesso.livre());
        when(usuarios.buscarPorEmail(Email.de("maria@exemplo.com"))).thenReturn(Optional.of(maria));
    }

    @Test
    void emailDesconhecidoEhRecusadoComoCredencialInvalidaEConfereSenhaFicticia() {
        var resultado = autenticar.executar(login("ninguem@exemplo.com", SENHA_CERTA, null));

        assertThat(resultado).isEqualTo(new Recusado(MotivoDaRecusa.CREDENCIAIS_INVALIDAS));
        verify(codificador).confere(SENHA_CERTA, new SenhaCriptografada("ficticia"));
        verify(eventos).registrar(argThat(e -> e.tipo() == TipoDeEventoDeSeguranca.LOGIN_FALHA && e.usuarioId() == null));
    }

    @Test
    void emailMalFormadoEhTratadoComoDesconhecido() {
        assertThat(autenticar.executar(login("nao-eh-email", SENHA_CERTA, null)))
                .isEqualTo(new Recusado(MotivoDaRecusa.CREDENCIAIS_INVALIDAS));
    }

    @Test
    void senhaErradaContaUmaFalha() {
        var resultado = autenticar.executar(login("maria@exemplo.com", "errada123", null));

        assertThat(resultado).isEqualTo(new Recusado(MotivoDaRecusa.CREDENCIAIS_INVALIDAS));
        assertThat(maria.controleDeAcesso().tentativasFalhas()).isEqualTo(1);
        verify(usuarios).salvar(maria);
    }

    @Test
    void quintaSenhaErradaBloqueia() {
        for (int i = 1; i < ControleDeAcesso.MAXIMO_DE_TENTATIVAS; i++) {
            autenticar.executar(login("maria@exemplo.com", "errada123", null));
        }

        var resultado = autenticar.executar(login("maria@exemplo.com", "errada123", null));

        assertThat(resultado).isEqualTo(new Recusado(MotivoDaRecusa.USUARIO_BLOQUEADO));
    }

    @Test
    void usuarioBloqueadoNaoEntraNemComASenhaCerta() {
        for (int i = 0; i < ControleDeAcesso.MAXIMO_DE_TENTATIVAS; i++) {
            maria.registrarFalhaDeLogin(AGORA);
        }

        var resultado = autenticar.executar(login("maria@exemplo.com", SENHA_CERTA, null));

        assertThat(resultado).isEqualTo(new Recusado(MotivoDaRecusa.USUARIO_BLOQUEADO));
        verify(emissor, never()).emitir(any(), any());
    }

    @Test
    void usuarioInativoNaoEntra() {
        maria.inativar();

        assertThat(autenticar.executar(login("maria@exemplo.com", SENHA_CERTA, null)))
                .isEqualTo(new Recusado(MotivoDaRecusa.USUARIO_INATIVO));
    }

    @Test
    void comUmaUnicaEmpresaEntraDireto() {
        acessoAs(EMPRESA_A);

        var resultado = autenticar.executar(login("maria@exemplo.com", SENHA_CERTA, null));

        assertThat(resultado).isEqualTo(new Autenticado(TOKENS, maria, EMPRESA_A));
        assertThat(maria.controleDeAcesso()).isEqualTo(ControleDeAcesso.livre());
    }

    @Test
    void comVariasEmpresasPedeParaEscolher() {
        acessoAs(EMPRESA_A, EMPRESA_B);

        var resultado = autenticar.executar(login("maria@exemplo.com", SENHA_CERTA, null));

        assertThat(resultado).isEqualTo(new SelecaoDeEmpresaNecessaria(List.of(EMPRESA_A, EMPRESA_B)));
        verify(emissor, never()).emitir(any(), any());
    }

    @Test
    void entraNaEmpresaEscolhida() {
        acessoAs(EMPRESA_A, EMPRESA_B);

        var resultado = autenticar.executar(login("maria@exemplo.com", SENHA_CERTA, 20L));

        assertThat(resultado).isEqualTo(new Autenticado(TOKENS, maria, EMPRESA_B));
    }

    @Test
    void recusaEmpresaSemVinculo() {
        acessoAs(EMPRESA_A);

        assertThat(autenticar.executar(login("maria@exemplo.com", SENHA_CERTA, 99L)))
                .isEqualTo(new Recusado(MotivoDaRecusa.SEM_ACESSO_A_EMPRESA));
    }

    @Test
    void semNenhumaEmpresaAtivaNaoEntra() {
        acessoAs();

        assertThat(autenticar.executar(login("maria@exemplo.com", SENHA_CERTA, null)))
                .isEqualTo(new Recusado(MotivoDaRecusa.SEM_ACESSO_A_EMPRESA));
    }

    @Test
    void administradorDaPlataformaEntraSemEmpresa() {
        Usuario admin = Usuario.reconstituir(2L, "Admin", Email.de("admin@goerp.com"), HASH, true, true,
                ControleDeAcesso.livre());
        when(usuarios.buscarPorEmail(Email.de("admin@goerp.com"))).thenReturn(Optional.of(admin));

        var resultado = autenticar.executar(login("admin@goerp.com", SENHA_CERTA, null));

        assertThat(resultado).isEqualTo(new Autenticado(TOKENS, admin, null));
        verify(emissor).emitir(eq(admin), isNull());
    }

    private void acessoAs(Empresa... empresas) {
        List<VinculoEmpresa> lista = java.util.Arrays.stream(empresas)
                .map(e -> VinculoEmpresa.reconstituir(e.id(), maria.id(), e.id(), 1L, true))
                .toList();
        when(vinculos.listarDoUsuario(maria.id())).thenReturn(lista);
        when(listarEmpresasAtivas.executar(anyCollection())).thenReturn(List.of(empresas));
    }

    private static SolicitacaoDeLogin login(String email, String senha, Long empresaId) {
        return new SolicitacaoDeLogin(email, senha, empresaId);
    }
}
