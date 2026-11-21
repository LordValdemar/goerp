package br.com.goerp.usuario.dominio;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

import br.com.goerp.compartilhado.dominio.Email;
import br.com.goerp.compartilhado.dominio.excecao.CampoObrigatorioException;

class UsuarioTest {

    private static final Email EMAIL = Email.de("maria@exemplo.com");
    private static final SenhaCriptografada SENHA = new SenhaCriptografada("hash-1");

    @Test
    void novoUsuarioNasceAtivoESemPrivilegioDePlataforma() {
        Usuario usuario = Usuario.novo("Maria", EMAIL, SENHA);

        assertThat(usuario.ativo()).isTrue();
        assertThat(usuario.administradorDaPlataforma()).isFalse();
    }

    @Test
    void administradorDaPlataformaTemPrivilegio() {
        Usuario admin = Usuario.novoAdministradorDaPlataforma("Admin", EMAIL, SENHA);

        assertThat(admin.administradorDaPlataforma()).isTrue();
    }

    @Test
    void exigeNome() {
        assertThatThrownBy(() -> Usuario.novo(" ", EMAIL, SENHA))
                .isInstanceOf(CampoObrigatorioException.class);
    }

    @Test
    void trocaSenha() {
        Usuario usuario = Usuario.novo("Maria", EMAIL, SENHA);

        usuario.trocarSenha(new SenhaCriptografada("hash-2"));

        assertThat(usuario.senha().hash()).isEqualTo("hash-2");
    }

    @Test
    void inativaEReativa() {
        Usuario usuario = Usuario.novo("Maria", EMAIL, SENHA);

        usuario.inativar();
        assertThat(usuario.ativo()).isFalse();

        usuario.ativar();
        assertThat(usuario.ativo()).isTrue();
    }
}
