package br.com.goerp.usuario.aplicacao.gestao;

import java.time.Clock;
import java.time.Instant;

import br.com.goerp.compartilhado.aplicacao.CasoDeUso;
import br.com.goerp.compartilhado.aplicacao.UsuarioAtual;
import br.com.goerp.usuario.aplicacao.CodificadorDeSenha;
import br.com.goerp.usuario.dominio.EventoDeSeguranca;
import br.com.goerp.usuario.dominio.EventoDeSegurancaRepositorio;
import br.com.goerp.usuario.dominio.SenhaAtualIncorretaException;
import br.com.goerp.usuario.dominio.Senha;
import br.com.goerp.usuario.dominio.SenhaRepetidaException;
import br.com.goerp.usuario.dominio.SessaoDeAcessoRepositorio;
import br.com.goerp.usuario.dominio.TipoDeEventoDeSeguranca;
import br.com.goerp.usuario.dominio.Usuario;
import br.com.goerp.usuario.dominio.UsuarioRepositorio;

/**
 * O próprio usuário troca a senha, informando a atual. Errar a senha atual conta como tentativa
 * falha (vale o mesmo bloqueio do login). Depois da troca, todas as sessões são encerradas:
 * quem tinha a senha antiga perde o acesso.
 */
@CasoDeUso
public class TrocarPropriaSenha {

    private final UsuarioRepositorio usuarios;
    private final SessaoDeAcessoRepositorio sessoes;
    private final EventoDeSegurancaRepositorio eventos;
    private final CodificadorDeSenha codificadorDeSenha;
    private final UsuarioAtual usuarioAtual;
    private final Clock relogio;

    public TrocarPropriaSenha(UsuarioRepositorio usuarios, SessaoDeAcessoRepositorio sessoes,
            EventoDeSegurancaRepositorio eventos, CodificadorDeSenha codificadorDeSenha, UsuarioAtual usuarioAtual,
            Clock relogio) {
        this.usuarios = usuarios;
        this.sessoes = sessoes;
        this.eventos = eventos;
        this.codificadorDeSenha = codificadorDeSenha;
        this.usuarioAtual = usuarioAtual;
        this.relogio = relogio;
    }

    /** Senha atual errada não lança exceção: a falha precisa ficar registrada (ver {@link ResultadoDaTrocaDeSenha}). */
    public ResultadoDaTrocaDeSenha executar(String senhaAtual, String novaSenha) {
        Instant agora = relogio.instant();
        Usuario usuario = usuarios.buscarPorId(usuarioAtual.id()).orElseThrow();

        if (usuario.estaBloqueado(agora) || !codificadorDeSenha.confere(senhaAtual, usuario.senha())) {
            usuario.registrarFalhaDeLogin(agora);
            usuarios.salvar(usuario);
            eventos.registrar(EventoDeSeguranca.doUsuario(TipoDeEventoDeSeguranca.LOGIN_FALHA, usuario, null, agora));
            return ResultadoDaTrocaDeSenha.SENHA_ATUAL_INCORRETA;
        }
        if (senhaAtual.equals(novaSenha)) {
            throw new SenhaRepetidaException();
        }
        usuario.trocarSenha(codificadorDeSenha.criptografar(new Senha(novaSenha)));
        usuario.registrarLoginComSucesso();
        usuarios.salvar(usuario);
        sessoes.revogarTodasDoUsuario(usuario.id(), agora);
        eventos.registrar(EventoDeSeguranca.doUsuario(TipoDeEventoDeSeguranca.SENHA_ALTERADA, usuario, null, agora));
        return ResultadoDaTrocaDeSenha.SENHA_ALTERADA;
    }

    /**
     * Senha atual errada é um resultado (não exceção) para a transação confirmar o registro da falha.
     * A camada web converte {@link #SENHA_ATUAL_INCORRETA} em {@link SenhaAtualIncorretaException}.
     */
    public enum ResultadoDaTrocaDeSenha {
        SENHA_ALTERADA,
        SENHA_ATUAL_INCORRETA
    }
}
