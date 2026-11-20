package br.com.goerp.cadastros.produto.dominio;

import static br.com.goerp.compartilhado.dominio.Textos.obrigatorio;

import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Unidade em que um produto é vendido (UN, KG, CX...). Cada empresa tem as suas.
 * A sigla vai na nota fiscal (até 6 caracteres) e não muda depois de criada.
 *
 * @see #fracionavel() se aceita quantidade com casas decimais (1,250 kg) ou só inteira (3 UN)
 */
public class UnidadeDeMedida {

    public static final int TAMANHO_MAXIMO_DA_SIGLA = 6;
    private static final Pattern FORMATO_DA_SIGLA = Pattern.compile("[A-Z0-9]{1," + TAMANHO_MAXIMO_DA_SIGLA + "}");

    private final Long id;
    private final Long empresaId;
    private final String sigla;
    private String descricao;
    private boolean fracionavel;
    private boolean ativa;

    private UnidadeDeMedida(Long id, Long empresaId, String sigla, String descricao, boolean fracionavel,
            boolean ativa) {
        this.id = id;
        this.empresaId = Objects.requireNonNull(empresaId, "empresaId");
        this.sigla = normalizarSigla(sigla);
        this.ativa = ativa;
        alterar(descricao, fracionavel);
    }

    public static UnidadeDeMedida nova(Long empresaId, String sigla, String descricao, boolean fracionavel) {
        return new UnidadeDeMedida(null, empresaId, sigla, descricao, fracionavel, true);
    }

    public static UnidadeDeMedida reconstituir(Long id, Long empresaId, String sigla, String descricao,
            boolean fracionavel, boolean ativa) {
        return new UnidadeDeMedida(id, empresaId, sigla, descricao, fracionavel, ativa);
    }

    /** A lista com que toda empresa começa. */
    public static List<UnidadeDeMedida> padroesDa(Long empresaId) {
        return List.of(
                nova(empresaId, "UN", "Unidade", false),
                nova(empresaId, "KG", "Quilograma", true),
                nova(empresaId, "G", "Grama", true),
                nova(empresaId, "L", "Litro", true),
                nova(empresaId, "ML", "Mililitro", true),
                nova(empresaId, "M", "Metro", true),
                nova(empresaId, "CX", "Caixa", false),
                nova(empresaId, "PC", "Peça", false),
                nova(empresaId, "PCT", "Pacote", false),
                nova(empresaId, "DZ", "Dúzia", false));
    }

    public static String normalizarSigla(String sigla) {
        String normalizada = sigla == null ? "" : sigla.strip().toUpperCase(Locale.ROOT);
        if (!FORMATO_DA_SIGLA.matcher(normalizada).matches()) {
            throw new SiglaInvalidaException(sigla);
        }
        return normalizada;
    }

    public void alterar(String novaDescricao, boolean aceitaFracao) {
        this.descricao = obrigatorio(novaDescricao, "descricao");
        this.fracionavel = aceitaFracao;
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

    public String sigla() {
        return sigla;
    }

    public String descricao() {
        return descricao;
    }

    public boolean fracionavel() {
        return fracionavel;
    }

    public boolean ativa() {
        return ativa;
    }
}
