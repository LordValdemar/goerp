package br.com.goerp.usuario.aplicacao;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;

import br.com.goerp.compartilhado.dominio.Email;
import br.com.goerp.usuario.dominio.SenhaCriptografada;
import br.com.goerp.usuario.dominio.SenhaFracaException;
import br.com.goerp.usuario.dominio.UsuarioRepositorio;

class CriarAdministradorDaPlataformaTest {

    UsuarioRepositorio usuarios = mock(UsuarioRepositorio.class);
    CodificadorDeSenha codificador = mock(CodificadorDeSenha.class);
    CriarAdministradorDaPlataforma criar = new CriarAdministradorDaPlataforma(usuarios, codificador);

    @Test
    void criaQuandoAindaNaoExisteNenhum() {
        when(codificador.criptografar(any())).thenReturn(new SenhaCriptografada("hash"));

        assertThat(criar.executar("Admin", "admin@goerp.local", "senhaForte1")).isTrue();
        verify(usuarios).salvar(argThat(usuario -> usuario.administradorDaPlataforma()));
    }

    @Test
    void naoFazNadaSeJaExiste() {
        when(usuarios.existeAdministradorDaPlataforma()).thenReturn(true);

        assertThat(criar.executar("Admin", "admin@goerp.local", "senhaForte1")).isFalse();
        verify(usuarios, never()).salvar(any());
    }

    @Test
    void recusaEmailDeUsuarioComum() {
        when(usuarios.existeComEmail(Email.de("maria@exemplo.com"))).thenReturn(true);

        assertThatThrownBy(() -> criar.executar("Admin", "maria@exemplo.com", "senhaForte1"))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void exigeSenhaForte() {
        assertThatThrownBy(() -> criar.executar("Admin", "admin@goerp.local", "123"))
                .isInstanceOf(SenhaFracaException.class);
    }
}
