package br.com.goerp.cadastros.produto.aplicacao;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import br.com.goerp.cadastros.produto.dominio.CategoriaRepositorio;
import br.com.goerp.cadastros.produto.dominio.CodigoDeBarrasEmUsoException;
import br.com.goerp.cadastros.produto.dominio.DadosDoProduto;
import br.com.goerp.cadastros.produto.dominio.DadosFiscais;
import br.com.goerp.cadastros.produto.dominio.ProdutoJaCadastradoException;
import br.com.goerp.cadastros.produto.dominio.ProdutoRepositorio;
import br.com.goerp.cadastros.produto.dominio.UnidadeDeMedida;
import br.com.goerp.cadastros.produto.dominio.UnidadeDeMedidaRepositorio;
import br.com.goerp.compartilhado.dominio.Dinheiro;
import br.com.goerp.compartilhado.dominio.Gtin;
import br.com.goerp.compartilhado.dominio.Ncm;
import br.com.goerp.compartilhado.dominio.excecao.RecursoNaoEncontradoException;

class CadastrarProdutoTest {

    private static final Gtin GTIN = Gtin.de("7891000315507");

    ProdutoRepositorio produtos = mock(ProdutoRepositorio.class);
    UnidadeDeMedidaRepositorio unidades = mock(UnidadeDeMedidaRepositorio.class);
    CategoriaRepositorio categorias = mock(CategoriaRepositorio.class);
    CadastrarProduto cadastrar = new CadastrarProduto(produtos, new BuscarUnidadeDeMedida(unidades),
            new BuscarCategoria(categorias), () -> 7L);

    @BeforeEach
    void preparar() {
        when(unidades.buscarPorId(10L)).thenReturn(Optional.of(UnidadeDeMedida.reconstituir(10L, 7L, "UN", "Unidade", false,
                true)));
        when(produtos.salvar(any())).thenAnswer(chamada -> chamada.getArgument(0));
    }

    @Test
    void cadastraNaEmpresaAtualComAUnidade() {
        var detalhado = cadastrar.executar("ref1", dados(10L));

        assertThat(detalhado.produto().empresaId()).isEqualTo(7L);
        assertThat(detalhado.produto().codigo()).isEqualTo("REF1");
        assertThat(detalhado.unidade().sigla()).isEqualTo("UN");
    }

    @Test
    void codigoRepetidoEhRecusadoMesmoComOutraCaixa() {
        when(produtos.existeComCodigo("REF1")).thenReturn(true);

        assertThatThrownBy(() -> cadastrar.executar("Ref1", dados(10L))).isInstanceOf(ProdutoJaCadastradoException.class);
        verify(produtos, never()).salvar(any());
    }

    @Test
    void codigoDeBarrasEmOutroProdutoEhRecusado() {
        when(produtos.codigoDeBarrasEmUso(GTIN, null)).thenReturn(true);

        assertThatThrownBy(() -> cadastrar.executar("REF1", dados(10L))).isInstanceOf(CodigoDeBarrasEmUsoException.class);
    }

    @Test
    void unidadeDeOutraEmpresaOuInexistenteDa404() {
        assertThatThrownBy(() -> cadastrar.executar("REF1", dados(99L))).isInstanceOf(RecursoNaoEncontradoException.class);
    }

    private static DadosDoProduto dados(Long unidadeId) {
        return new DadosDoProduto("Refrigerante", GTIN, null, unidadeId, Dinheiro.de("8.99"), null,
                new DadosFiscais(Ncm.de("22021000"), null, null), true, null);
    }
}
