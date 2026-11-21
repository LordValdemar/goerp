package br.com.goerp.usuario.infraestrutura.persistencia;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;

import br.com.goerp.compartilhado.dominio.Email;
import br.com.goerp.suporte.TesteDeIntegracao;
import br.com.goerp.usuario.aplicacao.CodificadorDeSenha;
import br.com.goerp.usuario.dominio.Senha;
import br.com.goerp.usuario.dominio.Usuario;
import br.com.goerp.usuario.dominio.UsuarioRepositorio;

class UsuarioRepositorioJpaTest extends TesteDeIntegracao {

    @Autowired
    UsuarioRepositorio repositorio;

    @Autowired
    CodificadorDeSenha codificador;

    @Test
    void buscaPorEmailSemDiferenciarMaiusculas() {
        repositorio.salvar(novoUsuario("maria@exemplo.com"));

        assertThat(repositorio.buscarPorEmail(Email.de("MARIA@Exemplo.com"))).isPresent();
        assertThat(repositorio.existeComEmail(Email.de("Maria@exemplo.com"))).isTrue();
    }

    @Test
    void bancoNaoGuardaASenhaEmTextoPuro() {
        Usuario salvo = repositorio.salvar(novoUsuario("maria@exemplo.com"));

        String gravado = jdbc.queryForObject("SELECT senha_hash FROM usuario WHERE id = ?", String.class, salvo.id());

        assertThat(gravado).isNotEqualTo("segredo123");
        assertThat(codificador.confere("segredo123", repositorio.buscarPorId(salvo.id()).orElseThrow().senha())).isTrue();
    }

    @Test
    void emailEhUnicoNoBanco() {
        repositorio.salvar(novoUsuario("maria@exemplo.com"));

        assertThatThrownBy(() -> repositorio.salvar(novoUsuario("maria@exemplo.com")))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void auditaAlteracoesSemGuardarOHashDaSenha() {
        Usuario usuario = repositorio.salvar(novoUsuario("maria@exemplo.com"));
        usuario.renomear("Maria Souza");
        usuario.inativar();
        repositorio.salvar(usuario);

        var historico = jdbc.queryForList("SELECT nome, ativo FROM usuario_aud WHERE id = ? ORDER BY rev", usuario.id());
        var colunas = jdbc.queryForList("""
                SELECT column_name FROM information_schema.columns WHERE table_name = 'usuario_aud'
                """, String.class);

        assertThat(historico).hasSize(2);
        assertThat(historico.get(1)).containsEntry("nome", "Maria Souza").containsEntry("ativo", false);
        assertThat(colunas).doesNotContain("senha_hash");
    }

    @Test
    void trocarSoASenhaNaoGeraRevisao() {
        Usuario usuario = repositorio.salvar(novoUsuario("maria@exemplo.com"));
        usuario.trocarSenha(codificador.criptografar(Senha.de("novaSenha456")));
        repositorio.salvar(usuario);

        Integer revisoes = jdbc.queryForObject("SELECT count(*) FROM usuario_aud WHERE id = ?", Integer.class, usuario.id());

        assertThat(revisoes).isEqualTo(1);
    }

    private Usuario novoUsuario(String email) {
        return Usuario.novo("Maria", Email.de(email), codificador.criptografar(Senha.de("segredo123")));
    }
}
