package br.com.goerp.usuario.aplicacao.autenticacao;

import static br.com.goerp.usuario.dominio.TipoDeEventoDeSeguranca.LOGIN_BLOQUEADO;
import static br.com.goerp.usuario.dominio.TipoDeEventoDeSeguranca.LOGIN_FALHA;
import static br.com.goerp.usuario.dominio.TipoDeEventoDeSeguranca.LOGIN_SUCESSO;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import br.com.goerp.compartilhado.aplicacao.AcessoEntreEmpresas;
import br.com.goerp.compartilhado.aplicacao.CasoDeUso;
import br.com.goerp.compartilhado.dominio.Email;
import br.com.goerp.compartilhado.dominio.excecao.ExcecaoDeDominio;
import br.com.goerp.empresa.aplicacao.ListarEmpresasAtivas;
import br.com.goerp.empresa.dominio.Empresa;
import br.com.goerp.usuario.aplicacao.CodificadorDeSenha;
import br.com.goerp.usuario.aplicacao.autenticacao.ResultadoDaAutenticacao.Autenticado;
import br.com.goerp.usuario.aplicacao.autenticacao.ResultadoDaAutenticacao.Recusado;
import br.com.goerp.usuario.aplicacao.autenticacao.ResultadoDaAutenticacao.SelecaoDeEmpresaNecessaria;
import br.com.goerp.usuario.dominio.EventoDeSeguranca;
import br.com.goerp.usuario.dominio.EventoDeSegurancaRepositorio;
import br.com.goerp.usuario.dominio.Senha;
import br.com.goerp.usuario.dominio.SenhaCriptografada;
import br.com.goerp.usuario.dominio.TipoDeEventoDeSeguranca;
import br.com.goerp.usuario.dominio.Usuario;
import br.com.goerp.usuario.dominio.UsuarioRepositorio;
import br.com.goerp.usuario.dominio.VinculoEmpresa;
import br.com.goerp.usuario.dominio.VinculoEmpresaRepositorio;

/** Login. Acessa todas as empresas porque precisa descobrir a quais delas o usuário tem acesso. */
@CasoDeUso
@AcessoEntreEmpresas
public class AutenticarUsuario {

    private final UsuarioRepositorio usuarios;
    private final VinculoEmpresaRepositorio vinculos;
    private final ListarEmpresasAtivas listarEmpresasAtivas;
    private final CodificadorDeSenha codificadorDeSenha;
    private final EmissorDeTokens emissorDeTokens;
    private final EventoDeSegurancaRepositorio eventos;
    private final Clock relogio;
    /**
     * Usado quando o e-mail não existe: conferir a senha mesmo assim faz a resposta levar o mesmo
     * tempo, e um atacante não descobre quais e-mails estão cadastrados medindo o tempo.
     */
    private final SenhaCriptografada senhaFicticia;

    public AutenticarUsuario(UsuarioRepositorio usuarios, VinculoEmpresaRepositorio vinculos,
            ListarEmpresasAtivas listarEmpresasAtivas, CodificadorDeSenha codificadorDeSenha,
            EmissorDeTokens emissorDeTokens, EventoDeSegurancaRepositorio eventos, Clock relogio) {
        this.usuarios = usuarios;
        this.vinculos = vinculos;
        this.listarEmpresasAtivas = listarEmpresasAtivas;
        this.codificadorDeSenha = codificadorDeSenha;
        this.emissorDeTokens = emissorDeTokens;
        this.eventos = eventos;
        this.relogio = relogio;
        this.senhaFicticia = codificadorDeSenha.criptografar(Senha.de("senhaFicticia123"));
    }

    public ResultadoDaAutenticacao executar(SolicitacaoDeLogin solicitacao) {
        Instant agora = relogio.instant();
        Optional<Usuario> encontrado = buscarUsuario(solicitacao.email());
        if (encontrado.isEmpty()) {
            codificadorDeSenha.confere(solicitacao.senha(), senhaFicticia);
            eventos.registrar(EventoDeSeguranca.deEmailDesconhecido(solicitacao.email(), agora));
            return new Recusado(MotivoDaRecusa.CREDENCIAIS_INVALIDAS);
        }
        Usuario usuario = encontrado.get();

        if (usuario.estaBloqueado(agora)) {
            registrar(LOGIN_BLOQUEADO, usuario, agora);
            return new Recusado(MotivoDaRecusa.USUARIO_BLOQUEADO);
        }
        if (!codificadorDeSenha.confere(solicitacao.senha(), usuario.senha())) {
            return registrarSenhaErrada(usuario, agora);
        }
        if (!usuario.ativo()) {
            registrar(LOGIN_FALHA, usuario, agora);
            return new Recusado(MotivoDaRecusa.USUARIO_INATIVO);
        }

        usuario.registrarLoginComSucesso();
        usuarios.salvar(usuario);
        return escolherEmpresa(usuario, solicitacao.empresaId(), agora);
    }

    private Optional<Usuario> buscarUsuario(String email) {
        try {
            return usuarios.buscarPorEmail(Email.de(email));
        } catch (ExcecaoDeDominio emailMalFormado) {
            return Optional.empty();
        }
    }

    private Recusado registrarSenhaErrada(Usuario usuario, Instant agora) {
        usuario.registrarFalhaDeLogin(agora);
        usuarios.salvar(usuario);
        if (usuario.estaBloqueado(agora)) {
            registrar(LOGIN_BLOQUEADO, usuario, agora);
            return new Recusado(MotivoDaRecusa.USUARIO_BLOQUEADO);
        }
        registrar(LOGIN_FALHA, usuario, agora);
        return new Recusado(MotivoDaRecusa.CREDENCIAIS_INVALIDAS);
    }

    private ResultadoDaAutenticacao escolherEmpresa(Usuario usuario, Long empresaSolicitada, Instant agora) {
        if (empresaSolicitada == null && usuario.administradorDaPlataforma()) {
            return autenticar(usuario, null, agora);
        }
        List<Empresa> acessiveis = empresasAcessiveis(usuario);

        if (empresaSolicitada != null) {
            return acessiveis.stream()
                    .filter(empresa -> empresa.id().equals(empresaSolicitada))
                    .findFirst()
                    .<ResultadoDaAutenticacao>map(empresa -> autenticar(usuario, empresa, agora))
                    .orElseGet(() -> semAcesso(usuario, agora));
        }
        return switch (acessiveis.size()) {
            case 0 -> semAcesso(usuario, agora);
            case 1 -> autenticar(usuario, acessiveis.getFirst(), agora);
            default -> new SelecaoDeEmpresaNecessaria(acessiveis);
        };
    }

    private List<Empresa> empresasAcessiveis(Usuario usuario) {
        List<Long> ids = vinculos.listarDoUsuario(usuario.id()).stream()
                .filter(VinculoEmpresa::ativo)
                .map(VinculoEmpresa::empresaId)
                .toList();
        return listarEmpresasAtivas.executar(ids);
    }

    private Autenticado autenticar(Usuario usuario, Empresa empresa, Instant agora) {
        TokensEmitidos tokens = emissorDeTokens.emitir(usuario, empresa);
        eventos.registrar(EventoDeSeguranca.doUsuario(LOGIN_SUCESSO, usuario, empresa == null ? null : empresa.id(), agora));
        return new Autenticado(tokens, usuario, empresa);
    }

    private Recusado semAcesso(Usuario usuario, Instant agora) {
        registrar(LOGIN_FALHA, usuario, agora);
        return new Recusado(MotivoDaRecusa.SEM_ACESSO_A_EMPRESA);
    }

    private void registrar(TipoDeEventoDeSeguranca tipo, Usuario usuario, Instant agora) {
        eventos.registrar(EventoDeSeguranca.doUsuario(tipo, usuario, null, agora));
    }
}
