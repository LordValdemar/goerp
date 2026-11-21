package br.com.goerp.usuario.dominio;

import java.util.List;
import java.util.Optional;

public interface VinculoEmpresaRepositorio {

    VinculoEmpresa salvar(VinculoEmpresa vinculo);

    Optional<VinculoEmpresa> buscar(Long usuarioId, Long empresaId);

    List<VinculoEmpresa> listarDoUsuario(Long usuarioId);

    boolean existeAtivoComPerfil(Long perfilId);

    /** Todos os vínculos da empresa atual. */
    List<VinculoEmpresa> listarDaEmpresa();

    long contarAtivosComPerfil(Long perfilId);
}
