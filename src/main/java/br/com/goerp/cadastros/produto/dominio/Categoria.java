package br.com.goerp.cadastros.produto.dominio;

import static br.com.goerp.compartilhado.dominio.Textos.obrigatorio;

import java.util.Objects;

/** Agrupamento de produtos definido por cada empresa (ex.: Bebidas, Limpeza). */
public class Categoria {

    private final Long id;
    private final Long empresaId;
    private String nome;
    private boolean ativa;

    private Categoria(Long id, Long empresaId, String nome, boolean ativa) {
        this.id = id;
        this.empresaId = Objects.requireNonNull(empresaId, "empresaId");
        this.ativa = ativa;
        renomear(nome);
    }

    public static Categoria nova(Long empresaId, String nome) {
        return new Categoria(null, empresaId, nome, true);
    }

    public static Categoria reconstituir(Long id, Long empresaId, String nome, boolean ativa) {
        return new Categoria(id, empresaId, nome, ativa);
    }

    public void renomear(String novoNome) {
        this.nome = obrigatorio(novoNome, "nome");
    }

    public void definirSituacao(boolean ativar) {
        this.ativa = ativar;
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

    public boolean ativa() {
        return ativa;
    }
}
