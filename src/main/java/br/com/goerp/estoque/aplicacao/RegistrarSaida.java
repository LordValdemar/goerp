package br.com.goerp.estoque.aplicacao;

import br.com.goerp.compartilhado.aplicacao.CasoDeUso;
import br.com.goerp.compartilhado.aplicacao.UsuarioAtual;
import br.com.goerp.compartilhado.dominio.Quantidade;
import br.com.goerp.estoque.dominio.Movimentacao;
import br.com.goerp.estoque.dominio.OrigemDaMovimentacao;
import br.com.goerp.estoque.dominio.PedidoDeMovimentacao;
import br.com.goerp.estoque.dominio.TipoDeMovimentacao;

/** Saída de mercadoria (venda, consumo interno, doação...). Sai pelo custo médio do momento. */
@CasoDeUso
public class RegistrarSaida {

    private final MovimentadorDeEstoque movimentador;
    private final UsuarioAtual usuarioAtual;

    public RegistrarSaida(MovimentadorDeEstoque movimentador, UsuarioAtual usuarioAtual) {
        this.movimentador = movimentador;
        this.usuarioAtual = usuarioAtual;
    }

    public Movimentacao executar(Long produtoId, Quantidade quantidade, OrigemDaMovimentacao origem,
            Long documentoDeOrigem, String observacao) {
        return movimentador.movimentar(produtoId, new PedidoDeMovimentacao(TipoDeMovimentacao.SAIDA, quantidade, null,
                origem, documentoDeOrigem, observacao, null, usuarioAtual.id()));
    }
}
