package br.com.goerp.usuario.dominio;

import static br.com.goerp.compartilhado.dominio.Textos.obrigatorio;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Objects;
import java.util.Set;

/**
 * Conjunto de permissões com um nome (ex.: "Caixa", "Gerente"), definido por cada empresa.
 *
 * <p>Toda empresa tem um perfil Administrador, criado pelo sistema: tem todas as permissões
 * (inclusive as que forem criadas no futuro) e não pode ser alterado nem inativado, para a
 * empresa nunca ficar sem alguém capaz de administrá-la.
 */
public class Perfil {

    public static final String NOME_DO_ADMINISTRADOR = "Administrador";

    private final Long id;
    private final Long empresaId;
    private final boolean administrador;
    private String nome;
    private Set<Permissao> permissoes;
    private boolean ativo;

    private Perfil(Long id, Long empresaId, String nome, Set<Permissao> permissoes, boolean administrador,
            boolean ativo) {
        this.id = id;
        this.empresaId = Objects.requireNonNull(empresaId, "empresaId");
        this.administrador = administrador;
        this.ativo = ativo;
        definir(nome, permissoes);
    }

    public static Perfil novo(Long empresaId, String nome, Set<Permissao> permissoes) {
        return new Perfil(null, empresaId, nome, permissoes, false, true);
    }

    public static Perfil administradorDa(Long empresaId) {
        return new Perfil(null, empresaId, NOME_DO_ADMINISTRADOR, EnumSet.allOf(Permissao.class), true, true);
    }

    public static Perfil reconstituir(Long id, Long empresaId, String nome, Set<Permissao> permissoes,
            boolean administrador, boolean ativo) {
        return new Perfil(id, empresaId, nome, permissoes, administrador, ativo);
    }

    public void alterar(String nome, Set<Permissao> permissoes) {
        exigirQueNaoSejaDoSistema();
        definir(nome, permissoes);
    }

    public void inativar() {
        exigirQueNaoSejaDoSistema();
        ativo = false;
    }

    public void ativar() {
        ativo = true;
    }

    public boolean concede(Permissao permissao) {
        return ativo && permissoes().contains(permissao);
    }

    /** O Administrador sempre tem todas as permissões existentes, mesmo as criadas depois dele. */
    public Set<Permissao> permissoes() {
        return administrador ? EnumSet.allOf(Permissao.class) : Collections.unmodifiableSet(permissoes);
    }

    private void definir(String nome, Set<Permissao> permissoes) {
        if (permissoes == null || permissoes.isEmpty()) {
            throw new PerfilSemPermissaoException();
        }
        this.nome = obrigatorio(nome, "nome");
        this.permissoes = EnumSet.copyOf(permissoes);
    }

    private void exigirQueNaoSejaDoSistema() {
        if (administrador) {
            throw new PerfilDoSistemaException();
        }
    }

    public Long id() {
        return id;
    }

    public Long empresaId() {
        return empresaId;
    }

    public String nome() {
        return nome;
    }

    public boolean administrador() {
        return administrador;
    }

    public boolean ativo() {
        return ativo;
    }
}
