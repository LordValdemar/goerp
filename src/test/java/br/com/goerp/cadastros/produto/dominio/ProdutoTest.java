package br.com.goerp.cadastros.produto.dominio;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;

import br.com.goerp.compartilhado.dominio.Dinheiro;
import br.com.goerp.compartilhado.dominio.Ncm;
import br.com.goerp.compartilhado.dominio.Quantidade;
import br.com.goerp.compartilhado.dominio.excecao.ValorInvalidoException;

class ProdutoTest {

    private static final Long EMPRESA = 1L;
    private static final UnidadeDeMedida UN = UnidadeDeMedida.reconstituir(10L, EMPRESA, "UN", "Unidade", false, true);
    private static final UnidadeDeMedida KG = UnidadeDeMedida.reconstituir(11L, EMPRESA, "KG", "Quilograma", true, true);
    private static final Categoria BEBIDAS = Categoria.reconstituir(20L, EMPRESA, "Bebidas", true);

    @Test
    void novoProdutoComCodigoNormalizadoEValoresPadrao() {
        Produto produto = Produto.novo(EMPRESA, " ref-001 ", dados(UN, "5.99", true, null), UN, BEBIDAS);

        assertThat(produto.codigo()).isEqualTo("REF-001");
        assertThat(produto.ativo()).isTrue();
        assertThat(produto.dados().precoDeCusto()).isEqualTo(CustoUnitario.ZERO);
        assertThat(produto.dados().estoqueMinimo()).isEqualTo(Quantidade.ZERO);
        assertThat(produto.dados().fiscais().origem()).isEqualTo(OrigemDaMercadoria.NACIONAL);
    }

    @Test
    void rejeitaCodigoInvalido() {
        assertThatThrownBy(() -> Produto.novo(EMPRESA, "COM ESPACO", dados(UN, "1", false, null), UN, null))
                .isInstanceOf(ValorInvalidoException.class);
        assertThatThrownBy(() -> Produto.novo(EMPRESA, "X".repeat(31), dados(UN, "1", false, null), UN, null))
                .isInstanceOf(ValorInvalidoException.class);
    }

    @Test
    void naoAceitaUnidadeOuCategoriaInativas() {
        UnidadeDeMedida inativa = UnidadeDeMedida.reconstituir(12L, EMPRESA, "FD", "Fardo", false, false);
        Categoria categoriaInativa = Categoria.reconstituir(21L, EMPRESA, "Antiga", false);

        assertThatThrownBy(() -> Produto.novo(EMPRESA, "A1", dados(inativa, "1", false, null), inativa, null))
                .isInstanceOf(UnidadeInativaException.class);
        assertThatThrownBy(() -> Produto.novo(EMPRESA, "A1", dados(UN, "1", false, null), UN, categoriaInativa))
                .isInstanceOf(CategoriaInativaException.class);
    }

    @Test
    void estoqueMinimoFracionadoSoEmUnidadeQueAceitaFracao() {
        assertThatThrownBy(() -> Produto.novo(EMPRESA, "A1", dados(UN, "1", true, "2.5"), UN, null))
                .isInstanceOf(EstoqueMinimoFracionadoException.class);

        assertThat(Produto.novo(EMPRESA, "A1", dados(KG, "1", true, "2.5"), KG, null).dados().estoqueMinimo())
                .isEqualTo(Quantidade.de("2.5"));
    }

    @Test
    void semControleDeEstoqueOMinimoEhZero() {
        assertThat(dados(UN, "1", false, "10").estoqueMinimo()).isEqualTo(Quantidade.ZERO);
    }

    @Test
    void precoNegativoEDescricaoLongaSaoRecusados() {
        assertThatThrownBy(() -> dados(UN, "-1", false, null)).isInstanceOf(ValorInvalidoException.class);
        assertThatThrownBy(() -> new DadosDoProduto("X".repeat(121), null, null, 10L, Dinheiro.de("1"), null,
                new DadosFiscais(Ncm.de("22021000"), null, null), false, null)).isInstanceOf(ValorInvalidoException.class);
    }

    @Test
    void custoComAteQuatroCasas() {
        assertThat(CustoUnitario.de("3.3333").valor()).isEqualTo(new BigDecimal("3.3333"));
        assertThatThrownBy(() -> CustoUnitario.de("3.33333")).isInstanceOf(ValorInvalidoException.class);
        assertThatThrownBy(() -> CustoUnitario.de("-1")).isInstanceOf(ValorInvalidoException.class);
    }

    @Test
    void alteracaoPodeManterUnidadeECategoriaQueForamInativadasDepois() {
        Produto produto = Produto.novo(EMPRESA, "A1", comCategoria(dados(UN, "1", false, null), BEBIDAS), UN, BEBIDAS);
        UnidadeDeMedida unInativada = UnidadeDeMedida.reconstituir(10L, EMPRESA, "UN", "Unidade", false, false);
        Categoria bebidasInativada = Categoria.reconstituir(20L, EMPRESA, "Bebidas", false);

        produto.alterar(comCategoria(dados(UN, "2", false, null), BEBIDAS), unInativada, bebidasInativada);

        assertThat(produto.dados().precoDeVenda()).isEqualTo(Dinheiro.de("2"));
    }

    @Test
    void alteracaoNaoAceitaTrocarParaUnidadeOuCategoriaInativas() {
        Produto produto = Produto.novo(EMPRESA, "A1", dados(UN, "1", false, null), UN, null);
        UnidadeDeMedida fardoInativo = UnidadeDeMedida.reconstituir(12L, EMPRESA, "FD", "Fardo", false, false);
        Categoria antiga = Categoria.reconstituir(21L, EMPRESA, "Antiga", false);

        assertThatThrownBy(() -> produto.alterar(dados(fardoInativo, "1", false, null), fardoInativo, null))
                .isInstanceOf(UnidadeInativaException.class);
        assertThatThrownBy(() -> produto.alterar(comCategoria(dados(UN, "1", false, null), antiga), UN, antiga))
                .isInstanceOf(CategoriaInativaException.class);
    }

    @Test
    void inativaEReativa() {
        Produto produto = Produto.novo(EMPRESA, "A1", dados(UN, "1", false, null), UN, null);

        produto.inativar();
        assertThat(produto.ativo()).isFalse();
        produto.ativar();
        assertThat(produto.ativo()).isTrue();
    }

    private static DadosDoProduto comCategoria(DadosDoProduto dados, Categoria categoria) {
        return new DadosDoProduto(dados.descricao(), dados.codigoDeBarras(), categoria.id(), dados.unidadeId(),
                dados.precoDeVenda(), dados.precoDeCusto(), dados.fiscais(), dados.controlaEstoque(),
                dados.estoqueMinimo());
    }

    private static DadosDoProduto dados(UnidadeDeMedida unidade, String preco, boolean controlaEstoque, String minimo) {
        return new DadosDoProduto("Refrigerante 2L", null, null, unidade.id(), Dinheiro.de(preco), null,
                new DadosFiscais(Ncm.de("22021000"), null, null), controlaEstoque,
                minimo == null ? null : Quantidade.de(minimo));
    }
}
