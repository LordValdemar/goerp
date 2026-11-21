package br.com.goerp.usuario.aplicacao.perfil;

import br.com.goerp.compartilhado.aplicacao.CasoDeUso;
import br.com.goerp.compartilhado.aplicacao.EmpresaAtual;
import br.com.goerp.usuario.dominio.Perfil;
import br.com.goerp.usuario.dominio.PerfilJaExisteException;
import br.com.goerp.usuario.dominio.PerfilRepositorio;

@CasoDeUso
public class CriarPerfil {

    private final PerfilRepositorio perfis;
    private final EmpresaAtual empresaAtual;

    public CriarPerfil(PerfilRepositorio perfis, EmpresaAtual empresaAtual) {
        this.perfis = perfis;
        this.empresaAtual = empresaAtual;
    }

    public Perfil executar(DadosDoPerfil dados) {
        Perfil perfil = Perfil.novo(empresaAtual.id(), dados.nome(), dados.permissoes());
        if (perfis.existeComNome(perfil.nome())) {
            throw new PerfilJaExisteException(perfil.nome());
        }
        return perfis.salvar(perfil);
    }
}
