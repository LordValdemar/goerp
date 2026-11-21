package br.com.goerp.usuario.dominio;

import static br.com.goerp.compartilhado.dominio.Textos.obrigatorio;

import java.time.Instant;
import java.util.Objects;

import br.com.goerp.compartilhado.dominio.Email;

/**
 * Pessoa que acessa o sistema. O usuário é global (um login), e o acesso a cada empresa
 * é dado por um {@link VinculoEmpresa}.
 */
public class Usuario {

    private final Long id;
    private final Email email;
    private final boolean administradorDaPlataforma;
    private String nome;
    private SenhaCriptografada senha;
    private boolean ativo;
    private ControleDeAcesso controleDeAcesso;

    private Usuario(Long id, String nome, Email email, SenhaCriptografada senha,
            boolean administradorDaPlataforma, boolean ativo, ControleDeAcesso controleDeAcesso) {
        this.id = id;
        this.email = Objects.requireNonNull(email, "email");
        this.senha = Objects.requireNonNull(senha, "senha");
        this.administradorDaPlataforma = administradorDaPlataforma;
        this.ativo = ativo;
        this.controleDeAcesso = Objects.requireNonNull(controleDeAcesso, "controleDeAcesso");
        renomear(nome);
    }

    public static Usuario novo(String nome, Email email, SenhaCriptografada senha) {
        return new Usuario(null, nome, email, senha, false, true, ControleDeAcesso.livre());
    }

    /** Administrador geral do sistema: cadastra empresas, não pertence a nenhuma. */
    public static Usuario novoAdministradorDaPlataforma(String nome, Email email, SenhaCriptografada senha) {
        return new Usuario(null, nome, email, senha, true, true, ControleDeAcesso.livre());
    }

    public static Usuario reconstituir(Long id, String nome, Email email, SenhaCriptografada senha,
            boolean administradorDaPlataforma, boolean ativo, ControleDeAcesso controleDeAcesso) {
        return new Usuario(id, nome, email, senha, administradorDaPlataforma, ativo, controleDeAcesso);
    }

    public boolean estaBloqueado(Instant agora) {
        return controleDeAcesso.bloqueadoEm(agora);
    }

    public void registrarFalhaDeLogin(Instant agora) {
        controleDeAcesso = controleDeAcesso.comFalhaEm(agora);
    }

    public void registrarLoginComSucesso() {
        controleDeAcesso = ControleDeAcesso.livre();
    }

    public void renomear(String nome) {
        this.nome = obrigatorio(nome, "nome");
    }

    public void trocarSenha(SenhaCriptografada novaSenha) {
        this.senha = Objects.requireNonNull(novaSenha, "novaSenha");
    }

    public void inativar() {
        ativo = false;
    }

    public void ativar() {
        ativo = true;
    }

    public Long id() {
        return id;
    }

    public String nome() {
        return nome;
    }

    public Email email() {
        return email;
    }

    public SenhaCriptografada senha() {
        return senha;
    }

    public boolean administradorDaPlataforma() {
        return administradorDaPlataforma;
    }

    public boolean ativo() {
        return ativo;
    }

    public ControleDeAcesso controleDeAcesso() {
        return controleDeAcesso;
    }
}
