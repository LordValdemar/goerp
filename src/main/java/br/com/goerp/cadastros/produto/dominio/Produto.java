package br.com.goerp.cadastros.produto.dominio;

import java.util.Locale;
import java.util.Objects;
import java.util.regex.Pattern;

import br.com.goerp.compartilhado.dominio.excecao.ValorInvalidoException;

/**
 * Produto vendido ou comprado pela empresa. O código interno (SKU) o identifica e não muda.
 *
 * <p>O saldo em estoque não fica aqui: é calculado pelas movimentações do módulo de estoque.
 */
public class Produto {

    public static final int TAMANHO_MAXIMO_DO_CODIGO = 30;
    private static final Pattern FORMATO_DO_CODIGO =
            Pattern.compile("[A-Z0-9._/-]{1," + TAMANHO_MAXIMO_DO_CODIGO + "}");

    private final Long id;
    private final Long empresaId;
    private final String codigo;
    private final Long versao;
    private DadosDoProduto dados;
    private boolean ativo;

    private Produto(Long id, Long empresaId, String codigo, DadosDoProduto dados, boolean ativo, Long versao) {
        this.id = id;
        this.empresaId = Objects.requireNonNull(empresaId, "empresaId");
        this.codigo = normalizarCodigo(codigo);
        this.dados = Objects.requireNonNull(dados, "dados");
        this.ativo = ativo;
        this.versao = versao;
    }

    /**
     * @param unidade a unidade escolhida: precisa estar ativa e, se não aceitar fração,
     *                o estoque mínimo precisa ser inteiro
     */
    public static Produto novo(Long empresaId, String codigo, DadosDoProduto dados, UnidadeDeMedida unidade,
            Categoria categoria) {
        exigirCompatibilidade(dados, unidade, categoria, null);
        return new Produto(null, empresaId, codigo, dados, true, null);
    }

    public static Produto reconstituir(Long id, Long empresaId, String codigo, DadosDoProduto dados, boolean ativo,
            Long versao) {
        return new Produto(id, empresaId, codigo, dados, ativo, versao);
    }

    /** Código interno em maiúsculas: letras, números e . _ / - (sem espaços). */
    public static String normalizarCodigo(String codigo) {
        String normalizado = codigo == null ? "" : codigo.strip().toUpperCase(Locale.ROOT);
        if (!FORMATO_DO_CODIGO.matcher(normalizado).matches()) {
            throw new ValorInvalidoException("Código do produto inválido (1 a " + TAMANHO_MAXIMO_DO_CODIGO
                    + " letras, números ou . _ / -): " + codigo);
        }
        return normalizado;
    }

    /**
     * Altera os dados. Unidade ou categoria inativas só são recusadas quando escolhidas agora:
     * um produto que já estava nelas pode continuar sendo alterado nos outros campos.
     */
    public void alterar(DadosDoProduto novosDados, UnidadeDeMedida unidade, Categoria categoria) {
        exigirCompatibilidade(novosDados, unidade, categoria, dados);
        this.dados = novosDados;
    }

    public void inativar() {
        ativo = false;
    }

    public void ativar() {
        ativo = true;
    }

    /** @param anteriores dados atuais do produto; nulo no cadastro */
    private static void exigirCompatibilidade(DadosDoProduto dados, UnidadeDeMedida unidade, Categoria categoria,
            DadosDoProduto anteriores) {
        if (!unidade.id().equals(dados.unidadeId())) {
            throw new IllegalArgumentException("Unidade informada não corresponde aos dados do produto");
        }
        boolean unidadeNova = anteriores == null || !unidade.id().equals(anteriores.unidadeId());
        if (unidadeNova && !unidade.ativa()) {
            throw new UnidadeInativaException(unidade.sigla());
        }
        if (!unidade.fracionavel() && !dados.estoqueMinimo().inteira()) {
            throw new EstoqueMinimoFracionadoException(unidade.sigla());
        }
        boolean categoriaNova = categoria != null
                && (anteriores == null || !categoria.id().equals(anteriores.categoriaId()));
        if (categoriaNova && !categoria.ativa()) {
            throw new CategoriaInativaException(categoria.nome());
        }
    }

    public Long id() {
        return id;
    }

    public Long empresaId() {
        return empresaId;
    }

    public String codigo() {
        return codigo;
    }

    public DadosDoProduto dados() {
        return dados;
    }

    public boolean ativo() {
        return ativo;
    }

    public Long versao() {
        return versao;
    }
}
