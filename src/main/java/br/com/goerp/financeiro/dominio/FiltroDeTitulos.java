package br.com.goerp.financeiro.dominio;

import java.time.LocalDate;
import java.util.Objects;

/**
 * Pesquisa de títulos de uma natureza. Demais filtros são opcionais (nulo não restringe).
 *
 * @param emAberto        só abertos e parcialmente pagos
 * @param vencidosAntesDe só os em aberto com vencimento anterior a esta data (os vencidos de hoje)
 */
public record FiltroDeTitulos(
        NaturezaDoTitulo natureza,
        Long pessoaId,
        SituacaoDoTitulo situacao,
        Boolean emAberto,
        LocalDate vencimentoDe,
        LocalDate vencimentoAte,
        LocalDate vencidosAntesDe) {

    public FiltroDeTitulos {
        Objects.requireNonNull(natureza, "natureza");
    }

    public static FiltroDeTitulos emAberto(NaturezaDoTitulo natureza) {
        return new FiltroDeTitulos(natureza, null, null, true, null, null, null);
    }

    public FiltroDeTitulos vencidosAntesDe(LocalDate dia) {
        return new FiltroDeTitulos(natureza, pessoaId, situacao, true, vencimentoDe, vencimentoAte, dia);
    }

    public FiltroDeTitulos vencendoEm(LocalDate dia) {
        return new FiltroDeTitulos(natureza, pessoaId, situacao, true, dia, dia, vencidosAntesDe);
    }
}
