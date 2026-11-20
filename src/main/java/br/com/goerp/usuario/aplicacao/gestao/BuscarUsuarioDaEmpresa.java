package br.com.goerp.usuario.aplicacao.gestao;

import br.com.goerp.compartilhado.aplicacao.CasoDeUso;

@CasoDeUso
public class BuscarUsuarioDaEmpresa {

    private final UsuariosDaEmpresa usuariosDaEmpresa;

    public BuscarUsuarioDaEmpresa(UsuariosDaEmpresa usuariosDaEmpresa) {
        this.usuariosDaEmpresa = usuariosDaEmpresa;
    }

    public UsuarioDaEmpresa executar(Long usuarioId) {
        return usuariosDaEmpresa.buscar(usuarioId);
    }
}
