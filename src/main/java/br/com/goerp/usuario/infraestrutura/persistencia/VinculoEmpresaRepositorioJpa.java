package br.com.goerp.usuario.infraestrutura.persistencia;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import br.com.goerp.compartilhado.dominio.excecao.RecursoNaoEncontradoException;
import br.com.goerp.usuario.dominio.VinculoEmpresa;
import br.com.goerp.usuario.dominio.VinculoEmpresaRepositorio;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
class VinculoEmpresaRepositorioJpa implements VinculoEmpresaRepositorio {

    private final VinculoEmpresaJpaRepository jpa;

    @Override
    public VinculoEmpresa salvar(VinculoEmpresa vinculo) {
        VinculoEmpresaJpa registro = vinculo.id() == null ? new VinculoEmpresaJpa() : buscarRegistro(vinculo.id());
        registro.setUsuarioId(vinculo.usuarioId());
        registro.setEmpresaId(vinculo.empresaId());
        registro.setPerfilId(vinculo.perfilId());
        registro.setAtivo(vinculo.ativo());
        return paraDominio(jpa.save(registro));
    }

    @Override
    public Optional<VinculoEmpresa> buscar(Long usuarioId, Long empresaId) {
        return jpa.findByUsuarioIdAndEmpresaId(usuarioId, empresaId).map(VinculoEmpresaRepositorioJpa::paraDominio);
    }

    @Override
    public List<VinculoEmpresa> listarDoUsuario(Long usuarioId) {
        return jpa.findByUsuarioIdOrderByEmpresaId(usuarioId).stream()
                .map(VinculoEmpresaRepositorioJpa::paraDominio)
                .toList();
    }

    @Override
    public boolean existeAtivoComPerfil(Long perfilId) {
        return jpa.existsByPerfilIdAndAtivoTrue(perfilId);
    }

    @Override
    public List<VinculoEmpresa> listarDaEmpresa() {
        return jpa.findAllByOrderById().stream().map(VinculoEmpresaRepositorioJpa::paraDominio).toList();
    }

    @Override
    public long contarAtivosComPerfil(Long perfilId) {
        return jpa.countByPerfilIdAndAtivoTrue(perfilId);
    }

    private VinculoEmpresaJpa buscarRegistro(Long id) {
        return jpa.findById(id).orElseThrow(() -> new RecursoNaoEncontradoException("Vínculo", id));
    }

    private static VinculoEmpresa paraDominio(VinculoEmpresaJpa registro) {
        return VinculoEmpresa.reconstituir(registro.getId(), registro.getUsuarioId(), registro.getEmpresaId(),
                registro.getPerfilId(), registro.isAtivo());
    }
}
