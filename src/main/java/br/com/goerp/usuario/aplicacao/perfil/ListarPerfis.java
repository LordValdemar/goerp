package br.com.goerp.usuario.aplicacao.perfil;

import java.util.List;

import br.com.goerp.compartilhado.aplicacao.CasoDeUso;
import br.com.goerp.usuario.dominio.Perfil;
import br.com.goerp.usuario.dominio.PerfilRepositorio;

@CasoDeUso
public class ListarPerfis {

    private final PerfilRepositorio perfis;

    public ListarPerfis(PerfilRepositorio perfis) {
        this.perfis = perfis;
    }

    public List<Perfil> executar() {
        return perfis.listar();
    }
}
