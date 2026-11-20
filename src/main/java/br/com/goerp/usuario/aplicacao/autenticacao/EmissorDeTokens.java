package br.com.goerp.usuario.aplicacao.autenticacao;

import java.time.Clock;
import java.time.Instant;
import java.util.Set;

import br.com.goerp.compartilhado.aplicacao.ServicoDeAplicacao;
import br.com.goerp.empresa.dominio.Empresa;
import br.com.goerp.usuario.dominio.SessaoDeAcesso;
import br.com.goerp.usuario.dominio.SessaoDeAcessoRepositorio;
import br.com.goerp.usuario.dominio.Usuario;

/** Abre uma sessão e emite o par token de acesso + token de renovação. */
@ServicoDeAplicacao
public class EmissorDeTokens {

    private final GeradorDeTokenDeAcesso geradorDeTokenDeAcesso;
    private final SessaoDeAcessoRepositorio sessoes;
    private final PoliticaDeSessao politica;
    private final Clock relogio;

    public EmissorDeTokens(GeradorDeTokenDeAcesso geradorDeTokenDeAcesso, SessaoDeAcessoRepositorio sessoes,
            PoliticaDeSessao politica, Clock relogio) {
        this.geradorDeTokenDeAcesso = geradorDeTokenDeAcesso;
        this.sessoes = sessoes;
        this.politica = politica;
        this.relogio = relogio;
    }

    public TokensEmitidos emitir(Usuario usuario, Empresa empresa) {
        Instant agora = relogio.instant();
        Long empresaId = empresa == null ? null : empresa.id();

        String tokenDeRenovacao = TokenDeRenovacao.gerar();
        SessaoDeAcesso sessao = sessoes.salvar(SessaoDeAcesso.abrir(usuario.id(), empresaId,
                TokenDeRenovacao.hash(tokenDeRenovacao), agora, politica.validadeDaRenovacao()));

        var dados = new DadosDoTokenDeAcesso(usuario.id(), usuario.nome(), empresaId, permissoesDe(usuario, empresa));
        TokenDeAcesso acesso = geradorDeTokenDeAcesso.gerar(dados, agora);
        return new TokensEmitidos(acesso, tokenDeRenovacao, sessao.expiraEm());
    }

    private static Set<String> permissoesDe(Usuario usuario, Empresa empresa) {
        if (empresa == null && usuario.administradorDaPlataforma()) {
            return Set.of(Permissoes.ADMINISTRAR_PLATAFORMA);
        }
        return Set.of();
    }
}
