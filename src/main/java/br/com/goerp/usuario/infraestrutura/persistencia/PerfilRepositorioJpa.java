package br.com.goerp.usuario.infraestrutura.persistencia;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import br.com.goerp.compartilhado.dominio.excecao.RecursoNaoEncontradoException;
import br.com.goerp.usuario.dominio.Perfil;
import br.com.goerp.usuario.dominio.PerfilRepositorio;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
class PerfilRepositorioJpa implements PerfilRepositorio {

    private final PerfilJpaRepository jpa;

    @Override
    public Perfil salvar(Perfil perfil) {
        PerfilJpa registro = perfil.id() == null ? new PerfilJpa() : buscarRegistro(perfil.id());
        registro.setEmpresaId(perfil.empresaId());
        registro.setNome(perfil.nome());
        registro.getPermissoes().clear();
        registro.getPermissoes().addAll(perfil.permissoes());
        registro.setAdministrador(perfil.administrador());
        registro.setAtivo(perfil.ativo());
        return paraDominio(jpa.save(registro));
    }

    @Override
    public Optional<Perfil> buscarPorId(Long id) {
        return jpa.findById(id).map(PerfilRepositorioJpa::paraDominio);
    }

    @Override
    public List<Perfil> listar() {
        return jpa.findAllByOrderByNome().stream().map(PerfilRepositorioJpa::paraDominio).toList();
    }

    @Override
    public boolean existeComNome(String nome) {
        return jpa.existsByNomeIgnoreCase(nome.strip());
    }

    @Override
    public boolean existeOutroComNome(String nome, Long idIgnorado) {
        return jpa.existsByNomeIgnoreCaseAndIdNot(nome.strip(), idIgnorado);
    }

    private PerfilJpa buscarRegistro(Long id) {
        return jpa.findById(id).orElseThrow(() -> new RecursoNaoEncontradoException("Perfil", id));
    }

    private static Perfil paraDominio(PerfilJpa registro) {
        return Perfil.reconstituir(registro.getId(), registro.getEmpresaId(), registro.getNome(),
                registro.getPermissoes(), registro.isAdministrador(), registro.isAtivo());
    }
}
