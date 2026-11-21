package br.com.goerp.usuario.aplicacao.gestao;

import java.util.List;

import br.com.goerp.compartilhado.aplicacao.CasoDeUso;

@CasoDeUso
public class ListarUsuariosDaEmpresa {

    private final UsuariosDaEmpresa usuariosDaEmpresa;

    public ListarUsuariosDaEmpresa(UsuariosDaEmpresa usuariosDaEmpresa) {
        this.usuariosDaEmpresa = usuariosDaEmpresa;
    }

    public List<UsuarioDaEmpresa> executar() {
        return usuariosDaEmpresa.listar();
    }
}
