package br.com.goerp.estoque.aplicacao;

import br.com.goerp.compartilhado.aplicacao.CasoDeUso;
import br.com.goerp.compartilhado.aplicacao.UsuarioAtual;
import br.com.goerp.compartilhado.dominio.Quantidade;
import br.com.goerp.estoque.dominio.Movimentacao;
import br.com.goerp.estoque.dominio.OrigemDaMovimentacao;
import br.com.goerp.estoque.dominio.PedidoDeMovimentacao;
import br.com.goerp.estoque.dominio.TipoDeMovimentacao;
import br.com.goerp.estoque.dominio.TipoDeMovimentacao.Sentido;

/** Correção de saldo fora do fluxo normal (quebra, perda, sobra). Exige motivo e não altera o custo médio. */
@CasoDeUso
public class AjustarEstoque {

    private final MovimentadorDeEstoque movimentador;
    private final UsuarioAtual usuarioAtual;

    public AjustarEstoque(MovimentadorDeEstoque movimentador, UsuarioAtual usuarioAtual) {
        this.movimentador = movimentador;
        this.usuarioAtual = usuarioAtual;
    }

    public Movimentacao executar(Long produtoId, Sentido sentido, Quantidade quantidade, String motivo) {
        TipoDeMovimentacao tipo = sentido == Sentido.ENTRADA ? TipoDeMovimentacao.AJUSTE_DE_ENTRADA
                : TipoDeMovimentacao.AJUSTE_DE_SAIDA;
        return movimentador.movimentar(produtoId, new PedidoDeMovimentacao(tipo, quantidade, null,
                OrigemDaMovimentacao.MANUAL, null, motivo, null, usuarioAtual.id()));
    }
}
