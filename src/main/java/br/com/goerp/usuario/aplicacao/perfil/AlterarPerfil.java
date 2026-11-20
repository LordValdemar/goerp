package br.com.goerp.usuario.aplicacao.perfil;

import br.com.goerp.compartilhado.aplicacao.CasoDeUso;
import br.com.goerp.usuario.dominio.Perfil;
import br.com.goerp.usuario.dominio.PerfilJaExisteException;
import br.com.goerp.usuario.dominio.PerfilRepositorio;

/** As novas permissões valem para os usuários a partir da próxima renovação do token (até 15 min). */
@CasoDeUso
public class AlterarPerfil {

    private final PerfilRepositorio perfis;
    private final BuscarPerfil buscarPerfil;

    public AlterarPerfil(PerfilRepositorio perfis, BuscarPerfil buscarPerfil) {
        this.perfis = perfis;
        this.buscarPerfil = buscarPerfil;
    }

    public Perfil executar(Long id, DadosDoPerfil dados) {
        Perfil perfil = buscarPerfil.executar(id);
        perfil.alterar(dados.nome(), dados.permissoes());
        if (perfis.existeOutroComNome(perfil.nome(), id)) {
            throw new PerfilJaExisteException(perfil.nome());
        }
        return perfis.salvar(perfil);
    }
}
