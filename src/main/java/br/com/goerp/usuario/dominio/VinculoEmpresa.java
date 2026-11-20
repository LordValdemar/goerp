package br.com.goerp.usuario.dominio;

import java.util.Objects;

/**
 * Dá a um usuário acesso a uma empresa, com um perfil de permissões.
 * O mesmo usuário pode ter vínculo com várias empresas, com perfis diferentes em cada uma.
 */
public class VinculoEmpresa {

    private final Long id;
    private final Long usuarioId;
    private final Long empresaId;
    private Long perfilId;
    private boolean ativo;

    private VinculoEmpresa(Long id, Long usuarioId, Long empresaId, Long perfilId, boolean ativo) {
        this.id = id;
        this.usuarioId = Objects.requireNonNull(usuarioId, "usuarioId");
        this.empresaId = Objects.requireNonNull(empresaId, "empresaId");
        this.perfilId = Objects.requireNonNull(perfilId, "perfilId");
        this.ativo = ativo;
    }

    public static VinculoEmpresa novo(Long usuarioId, Long empresaId, Long perfilId) {
        return new VinculoEmpresa(null, usuarioId, empresaId, perfilId, true);
    }

    public static VinculoEmpresa reconstituir(Long id, Long usuarioId, Long empresaId, Long perfilId, boolean ativo) {
        return new VinculoEmpresa(id, usuarioId, empresaId, perfilId, ativo);
    }

    public void trocarPerfil(Long perfilId) {
        this.perfilId = Objects.requireNonNull(perfilId, "perfilId");
    }

    public void inativar() {
        ativo = false;
    }

    public void ativar() {
        ativo = true;
    }

    public Long id() {
        return id;
    }

    public Long usuarioId() {
        return usuarioId;
    }

    public Long empresaId() {
        return empresaId;
    }

    public Long perfilId() {
        return perfilId;
    }

    public boolean ativo() {
        return ativo;
    }
}
