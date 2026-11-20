package br.com.goerp.usuario.dominio;

import java.util.Objects;

/** Dá a um usuário acesso a uma empresa. O mesmo usuário pode ter vínculo com várias empresas. */
public class VinculoEmpresa {

    private final Long id;
    private final Long usuarioId;
    private final Long empresaId;
    private boolean ativo;

    private VinculoEmpresa(Long id, Long usuarioId, Long empresaId, boolean ativo) {
        this.id = id;
        this.usuarioId = Objects.requireNonNull(usuarioId, "usuarioId");
        this.empresaId = Objects.requireNonNull(empresaId, "empresaId");
        this.ativo = ativo;
    }

    public static VinculoEmpresa novo(Long usuarioId, Long empresaId) {
        return new VinculoEmpresa(null, usuarioId, empresaId, true);
    }

    public static VinculoEmpresa reconstituir(Long id, Long usuarioId, Long empresaId, boolean ativo) {
        return new VinculoEmpresa(id, usuarioId, empresaId, ativo);
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

    public boolean ativo() {
        return ativo;
    }
}
