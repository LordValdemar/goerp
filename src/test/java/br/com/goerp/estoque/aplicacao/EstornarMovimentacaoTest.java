package br.com.goerp.estoque.aplicacao;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import br.com.goerp.cadastros.produto.dominio.CustoUnitario;
import br.com.goerp.compartilhado.dominio.Quantidade;
import br.com.goerp.compartilhado.dominio.excecao.RecursoNaoEncontradoException;
import br.com.goerp.estoque.dominio.Movimentacao;
import br.com.goerp.estoque.dominio.MovimentacaoJaEstornadaException;
import br.com.goerp.estoque.dominio.MovimentacaoRepositorio;
import br.com.goerp.estoque.dominio.OrigemDaMovimentacao;
import br.com.goerp.estoque.dominio.TipoDeMovimentacao;

class EstornarMovimentacaoTest {

    MovimentacaoRepositorio movimentacoes = mock(MovimentacaoRepositorio.class);
    MovimentadorDeEstoque movimentador = mock(MovimentadorDeEstoque.class);
    EstornarMovimentacao estornar = new EstornarMovimentacao(movimentacoes, movimentador, () -> 1L);

    @Test
    void naoEstornaDuasVezes() {
        when(movimentacoes.buscarPorId(5L)).thenReturn(Optional.of(entrada()));
        when(movimentacoes.foiEstornada(5L)).thenReturn(true);

        assertThatThrownBy(() -> estornar.executar(5L, "Erro")).isInstanceOf(MovimentacaoJaEstornadaException.class);
        verify(movimentador, never()).movimentar(any(), any());
    }

    @Test
    void movimentacaoInexistenteOuDeOutraEmpresaDa404() {
        assertThatThrownBy(() -> estornar.executar(99L, "Erro")).isInstanceOf(RecursoNaoEncontradoException.class);
    }

    @Test
    void estornaComOPedidoInverso() {
        when(movimentacoes.buscarPorId(5L)).thenReturn(Optional.of(entrada()));

        estornar.executar(5L, "Nota em dobro");

        verify(movimentador).movimentar(any(), org.mockito.ArgumentMatchers.argThat(pedido ->
                pedido.tipo() == TipoDeMovimentacao.ESTORNO_DE_ENTRADA && pedido.estornada().equals(5L)));
    }

    private static Movimentacao entrada() {
        return new Movimentacao(5L, 1L, 10L, TipoDeMovimentacao.ENTRADA, Quantidade.de("10"), CustoUnitario.de("5"),
                Quantidade.de("10"), CustoUnitario.de("5"), OrigemDaMovimentacao.MANUAL, null, null, null, 1L,
                Instant.now());
    }
}
