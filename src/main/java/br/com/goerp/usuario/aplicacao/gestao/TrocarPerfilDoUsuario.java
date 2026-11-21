package br.com.goerp.usuario.aplicacao.gestao;

import br.com.goerp.compartilhado.aplicacao.CasoDeUso;
import br.com.goerp.usuario.dominio.Perfil;
import br.com.goerp.usuario.dominio.VinculoEmpresaRepositorio;

/** O novo perfil vale a partir da próxima renovação do token do usuário (até 15 min). */
@CasoDeUso
public class TrocarPerfilDoUsuario {

    private final VinculoEmpresaRepositorio vinculos;
    private final UsuariosDaEmpresa usuariosDaEmpresa;

    public TrocarPerfilDoUsuario(VinculoEmpresaRepositorio vinculos, UsuariosDaEmpresa usuariosDaEmpresa) {
        this.vinculos = vinculos;
        this.usuariosDaEmpresa = usuariosDaEmpresa;
    }

    public UsuarioDaEmpresa executar(Long usuarioId, Long perfilId) {
        UsuarioDaEmpresa atual = usuariosDaEmpresa.buscar(usuarioId);
        Perfil novoPerfil = usuariosDaEmpresa.perfilAtivo(perfilId);
        if (!novoPerfil.administrador()) {
            usuariosDaEmpresa.garantirQueRestaOutroAdministrador(atual);
        }
        atual.vinculo().trocarPerfil(novoPerfil.id());
        return new UsuarioDaEmpresa(atual.usuario(), vinculos.salvar(atual.vinculo()), novoPerfil);
    }
}
