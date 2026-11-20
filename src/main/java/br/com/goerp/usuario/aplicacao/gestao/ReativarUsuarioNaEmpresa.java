package br.com.goerp.usuario.aplicacao.gestao;

import br.com.goerp.compartilhado.aplicacao.CasoDeUso;
import br.com.goerp.usuario.dominio.VinculoEmpresaRepositorio;

@CasoDeUso
public class ReativarUsuarioNaEmpresa {

    private final VinculoEmpresaRepositorio vinculos;
    private final UsuariosDaEmpresa usuariosDaEmpresa;

    public ReativarUsuarioNaEmpresa(VinculoEmpresaRepositorio vinculos, UsuariosDaEmpresa usuariosDaEmpresa) {
        this.vinculos = vinculos;
        this.usuariosDaEmpresa = usuariosDaEmpresa;
    }

    public UsuarioDaEmpresa executar(Long usuarioId) {
        UsuarioDaEmpresa atual = usuariosDaEmpresa.buscar(usuarioId);
        usuariosDaEmpresa.perfilAtivo(atual.perfil().id());
        atual.vinculo().ativar();
        return new UsuarioDaEmpresa(atual.usuario(), vinculos.salvar(atual.vinculo()), atual.perfil());
    }
}
