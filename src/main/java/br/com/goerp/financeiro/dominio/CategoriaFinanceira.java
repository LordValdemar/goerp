package br.com.goerp.financeiro.dominio;

import static br.com.goerp.compartilhado.dominio.Textos.obrigatorio;

import java.util.Objects;

/** Plano de contas simples: para que serviu cada entrada ou saída (aluguel, energia, vendas...). */
public class CategoriaFinanceira {

    private final Long id;
    private final Long empresaId;
    private final TipoDeCategoria tipo;
    private String nome;
    private boolean ativa;

    private CategoriaFinanceira(Long id, Long empresaId, TipoDeCategoria tipo, String nome, boolean ativa) {
        this.id = id;
        this.empresaId = Objects.requireNonNull(empresaId, "empresaId");
        this.tipo = Objects.requireNonNull(tipo, "tipo");
        this.ativa = ativa;
        renomear(nome);
    }

    public static CategoriaFinanceira nova(Long empresaId, TipoDeCategoria tipo, String nome) {
        return new CategoriaFinanceira(null, empresaId, tipo, nome, true);
    }

    public static CategoriaFinanceira reconstituir(Long id, Long empresaId, TipoDeCategoria tipo, String nome,
            boolean ativa) {
        return new CategoriaFinanceira(id, empresaId, tipo, nome, ativa);
    }

    public void renomear(String novoNome) {
        this.nome = obrigatorio(novoNome, "nome");
    }

    public void definirSituacao(boolean ativar) {
        this.ativa = ativar;
    }

    /** Uma receita só classifica entradas; uma despesa, só saídas. */
    public void exigirQueClassifica(TipoDeLancamento.Sentido sentido) {
        if (!ativa) {
            throw new CategoriaInativaException(nome);
        }
        if (!tipo.aceita(sentido)) {
            throw new CategoriaIncompativelException(nome, tipo);
        }
    }

    public Long id() {
        return id;
    }

    public Long empresaId() {
        return empresaId;
    }

    public TipoDeCategoria tipo() {
        return tipo;
    }

    public String nome() {
        return nome;
    }

    public boolean ativa() {
        return ativa;
    }
}
