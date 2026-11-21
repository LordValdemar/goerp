package br.com.goerp.financeiro.dominio;

import static br.com.goerp.financeiro.dominio.TipoDeLancamento.Sentido.ENTRADA;
import static br.com.goerp.financeiro.dominio.TipoDeLancamento.Sentido.SAIDA;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class CategoriaFinanceiraTest {

    @Test
    void receitaSoClassificaEntradaEDespesaSoSaida() {
        CategoriaFinanceira vendas = CategoriaFinanceira.nova(1L, TipoDeCategoria.RECEITA, "Vendas");
        CategoriaFinanceira aluguel = CategoriaFinanceira.nova(1L, TipoDeCategoria.DESPESA, "Aluguel");

        assertThatCode(() -> vendas.exigirQueClassifica(ENTRADA)).doesNotThrowAnyException();
        assertThatCode(() -> aluguel.exigirQueClassifica(SAIDA)).doesNotThrowAnyException();
        assertThatThrownBy(() -> aluguel.exigirQueClassifica(ENTRADA))
                .isInstanceOf(CategoriaIncompativelException.class)
                .hasMessage("A categoria Aluguel é de despesa e não serve para este lançamento");
    }

    @Test
    void inativaNaoClassifica() {
        CategoriaFinanceira energia = CategoriaFinanceira.nova(1L, TipoDeCategoria.DESPESA, "Energia");
        energia.definirSituacao(false);

        assertThatThrownBy(() -> energia.exigirQueClassifica(SAIDA)).isInstanceOf(CategoriaInativaException.class);
    }
}
