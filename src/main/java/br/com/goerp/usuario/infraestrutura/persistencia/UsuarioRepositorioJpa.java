package br.com.goerp.usuario.infraestrutura.persistencia;

import java.util.Optional;

import org.springframework.stereotype.Repository;

import br.com.goerp.compartilhado.dominio.Email;
import br.com.goerp.compartilhado.dominio.excecao.RecursoNaoEncontradoException;
import br.com.goerp.usuario.dominio.ControleDeAcesso;
import br.com.goerp.usuario.dominio.SenhaCriptografada;
import br.com.goerp.usuario.dominio.Usuario;
import br.com.goerp.usuario.dominio.UsuarioRepositorio;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
class UsuarioRepositorioJpa implements UsuarioRepositorio {

    private final UsuarioJpaRepository jpa;

    @Override
    public Usuario salvar(Usuario usuario) {
        UsuarioJpa registro = usuario.id() == null ? new UsuarioJpa() : buscarRegistro(usuario.id());
        copiarParaRegistro(usuario, registro);
        return paraDominio(jpa.save(registro));
    }

    @Override
    public Optional<Usuario> buscarPorId(Long id) {
        return jpa.findById(id).map(UsuarioRepositorioJpa::paraDominio);
    }

    @Override
    public Optional<Usuario> buscarPorEmail(Email email) {
        return jpa.findByEmail(email).map(UsuarioRepositorioJpa::paraDominio);
    }

    @Override
    public boolean existeComEmail(Email email) {
        return jpa.existsByEmail(email);
    }

    private UsuarioJpa buscarRegistro(Long id) {
        return jpa.findById(id).orElseThrow(() -> new RecursoNaoEncontradoException("Usuário", id));
    }

    private static void copiarParaRegistro(Usuario usuario, UsuarioJpa registro) {
        registro.setNome(usuario.nome());
        registro.setEmail(usuario.email());
        registro.setSenhaHash(usuario.senha().hash());
        registro.setAdministradorDaPlataforma(usuario.administradorDaPlataforma());
        registro.setAtivo(usuario.ativo());
        registro.setTentativasFalhas(usuario.controleDeAcesso().tentativasFalhas());
        registro.setBloqueadoAte(usuario.controleDeAcesso().bloqueadoAte());
    }

    private static Usuario paraDominio(UsuarioJpa registro) {
        return Usuario.reconstituir(registro.getId(), registro.getNome(), registro.getEmail(),
                new SenhaCriptografada(registro.getSenhaHash()), registro.isAdministradorDaPlataforma(),
                registro.isAtivo(), new ControleDeAcesso(registro.getTentativasFalhas(), registro.getBloqueadoAte()));
    }
}
