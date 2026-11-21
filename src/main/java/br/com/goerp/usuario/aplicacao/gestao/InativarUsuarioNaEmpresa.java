package br.com.goerp.usuario.aplicacao.gestao;

import java.time.Clock;

import br.com.goerp.compartilhado.aplicacao.CasoDeUso;
import br.com.goerp.compartilhado.aplicacao.EmpresaAtual;
import br.com.goerp.compartilhado.aplicacao.UsuarioAtual;
import br.com.goerp.usuario.dominio.AutoInativacaoException;
import br.com.goerp.usuario.dominio.SessaoDeAcessoRepositorio;
import br.com.goerp.usuario.dominio.VinculoEmpresaRepositorio;

/**
 * Remove o acesso do usuário a esta empresa (o usuário continua existindo e acessando outras).
 * As sessões dele nesta empresa são encerradas na hora: ele não consegue renovar o token.
 */
@CasoDeUso
public class InativarUsuarioNaEmpresa {

    private final VinculoEmpresaRepositorio vinculos;
    private final SessaoDeAcessoRepositorio sessoes;
    private final UsuariosDaEmpresa usuariosDaEmpresa;
    private final UsuarioAtual usuarioAtual;
    private final EmpresaAtual empresaAtual;
    private final Clock relogio;

    public InativarUsuarioNaEmpresa(VinculoEmpresaRepositorio vinculos, SessaoDeAcessoRepositorio sessoes,
            UsuariosDaEmpresa usuariosDaEmpresa, UsuarioAtual usuarioAtual, EmpresaAtual empresaAtual, Clock relogio) {
        this.vinculos = vinculos;
        this.sessoes = sessoes;
        this.usuariosDaEmpresa = usuariosDaEmpresa;
        this.usuarioAtual = usuarioAtual;
        this.empresaAtual = empresaAtual;
        this.relogio = relogio;
    }

    public void executar(Long usuarioId) {
        if (usuarioId.equals(usuarioAtual.id())) {
            throw new AutoInativacaoException();
        }
        UsuarioDaEmpresa atual = usuariosDaEmpresa.buscar(usuarioId);
        usuariosDaEmpresa.garantirQueRestaOutroAdministrador(atual);
        atual.vinculo().inativar();
        vinculos.salvar(atual.vinculo());
        sessoes.revogarTodasDoUsuarioNaEmpresa(usuarioId, empresaAtual.id(), relogio.instant());
    }
}
