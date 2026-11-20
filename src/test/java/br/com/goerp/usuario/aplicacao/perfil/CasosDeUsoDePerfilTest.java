package br.com.goerp.usuario.aplicacao.perfil;

import static br.com.goerp.usuario.dominio.Permissao.USUARIO_LER;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.Test;

import br.com.goerp.compartilhado.dominio.excecao.RecursoNaoEncontradoException;
import br.com.goerp.usuario.dominio.Perfil;
import br.com.goerp.usuario.dominio.PerfilEmUsoException;
import br.com.goerp.usuario.dominio.PerfilJaExisteException;
import br.com.goerp.usuario.dominio.PerfilRepositorio;
import br.com.goerp.usuario.dominio.VinculoEmpresaRepositorio;

class CasosDeUsoDePerfilTest {

    private static final Long EMPRESA = 7L;
    private static final DadosDoPerfil CAIXA = new DadosDoPerfil("Caixa", Set.of(USUARIO_LER));

    PerfilRepositorio perfis = mock(PerfilRepositorio.class);
    VinculoEmpresaRepositorio vinculos = mock(VinculoEmpresaRepositorio.class);
    BuscarPerfil buscar = new BuscarPerfil(perfis);

    @Test
    void criaPerfilNaEmpresaAtual() {
        when(perfis.salvar(any())).thenAnswer(chamada -> chamada.getArgument(0));

        Perfil criado = new CriarPerfil(perfis, () -> EMPRESA).executar(CAIXA);

        assertThat(criado.empresaId()).isEqualTo(EMPRESA);
    }

    @Test
    void naoCriaPerfilComNomeRepetido() {
        when(perfis.existeComNome("Caixa")).thenReturn(true);

        assertThatThrownBy(() -> new CriarPerfil(perfis, () -> EMPRESA).executar(CAIXA))
                .isInstanceOf(PerfilJaExisteException.class);
        verify(perfis, never()).salvar(any());
    }

    @Test
    void naoRenomeiaParaNomeDeOutroPerfil() {
        when(perfis.buscarPorId(1L)).thenReturn(Optional.of(Perfil.reconstituir(1L, EMPRESA, "Estoquista",
                Set.of(USUARIO_LER), false, true)));
        when(perfis.existeOutroComNome("Caixa", 1L)).thenReturn(true);

        assertThatThrownBy(() -> new AlterarPerfil(perfis, buscar).executar(1L, CAIXA))
                .isInstanceOf(PerfilJaExisteException.class);
    }

    @Test
    void naoInativaPerfilEmUso() {
        when(perfis.buscarPorId(1L)).thenReturn(Optional.of(Perfil.reconstituir(1L, EMPRESA, "Caixa",
                Set.of(USUARIO_LER), false, true)));
        when(vinculos.existeAtivoComPerfil(1L)).thenReturn(true);

        assertThatThrownBy(() -> new InativarPerfil(perfis, vinculos, buscar).executar(1L))
                .isInstanceOf(PerfilEmUsoException.class);
        verify(perfis, never()).salvar(any());
    }

    @Test
    void perfilInexistenteDa404() {
        assertThatThrownBy(() -> buscar.executar(99L)).isInstanceOf(RecursoNaoEncontradoException.class);
    }
}
