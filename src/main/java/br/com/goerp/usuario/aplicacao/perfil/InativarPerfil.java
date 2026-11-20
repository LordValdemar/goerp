package br.com.goerp.usuario.aplicacao.perfil;

import br.com.goerp.compartilhado.aplicacao.CasoDeUso;
import br.com.goerp.usuario.dominio.Perfil;
import br.com.goerp.usuario.dominio.PerfilEmUsoException;
import br.com.goerp.usuario.dominio.PerfilRepositorio;
import br.com.goerp.usuario.dominio.VinculoEmpresaRepositorio;

@CasoDeUso
public class InativarPerfil {

    private final PerfilRepositorio perfis;
    private final VinculoEmpresaRepositorio vinculos;
    private final BuscarPerfil buscarPerfil;

    public InativarPerfil(PerfilRepositorio perfis, VinculoEmpresaRepositorio vinculos, BuscarPerfil buscarPerfil) {
        this.perfis = perfis;
        this.vinculos = vinculos;
        this.buscarPerfil = buscarPerfil;
    }

    public void executar(Long id) {
        Perfil perfil = buscarPerfil.executar(id);
        if (vinculos.existeAtivoComPerfil(id)) {
            throw new PerfilEmUsoException();
        }
        perfil.inativar();
        perfis.salvar(perfil);
    }
}
