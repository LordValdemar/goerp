package br.com.goerp.usuario.aplicacao.perfil;

import br.com.goerp.compartilhado.aplicacao.CasoDeUso;
import br.com.goerp.compartilhado.dominio.excecao.RecursoNaoEncontradoException;
import br.com.goerp.usuario.dominio.Perfil;
import br.com.goerp.usuario.dominio.PerfilRepositorio;

@CasoDeUso
public class BuscarPerfil {

    private final PerfilRepositorio perfis;

    public BuscarPerfil(PerfilRepositorio perfis) {
        this.perfis = perfis;
    }

    public Perfil executar(Long id) {
        return perfis.buscarPorId(id).orElseThrow(() -> new RecursoNaoEncontradoException("Perfil", id));
    }
}
