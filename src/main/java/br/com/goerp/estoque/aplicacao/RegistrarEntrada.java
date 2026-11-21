package br.com.goerp.estoque.aplicacao;

import br.com.goerp.cadastros.produto.dominio.CustoUnitario;
import br.com.goerp.compartilhado.aplicacao.CasoDeUso;
import br.com.goerp.compartilhado.aplicacao.UsuarioAtual;
import br.com.goerp.compartilhado.dominio.Quantidade;
import br.com.goerp.estoque.dominio.Movimentacao;
import br.com.goerp.estoque.dominio.OrigemDaMovimentacao;
import br.com.goerp.estoque.dominio.PedidoDeMovimentacao;
import br.com.goerp.estoque.dominio.TipoDeMovimentacao;

/** Entrada de mercadoria (manual ou vinda de uma compra). O custo informado entra no custo médio. */
@CasoDeUso
public class RegistrarEntrada {

    private final MovimentadorDeEstoque movimentador;
    private final UsuarioAtual usuarioAtual;

    public RegistrarEntrada(MovimentadorDeEstoque movimentador, UsuarioAtual usuarioAtual) {
        this.movimentador = movimentador;
        this.usuarioAtual = usuarioAtual;
    }

    public Movimentacao executar(Long produtoId, Quantidade quantidade, CustoUnitario custo, OrigemDaMovimentacao origem,
            Long documentoDeOrigem, String observacao) {
        return movimentador.movimentar(produtoId, new PedidoDeMovimentacao(TipoDeMovimentacao.ENTRADA, quantidade,
                custo, origem, documentoDeOrigem, observacao, null, usuarioAtual.id()));
    }
}
