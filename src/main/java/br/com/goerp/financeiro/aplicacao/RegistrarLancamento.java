package br.com.goerp.financeiro.aplicacao;

import br.com.goerp.compartilhado.aplicacao.CasoDeUso;
import br.com.goerp.compartilhado.aplicacao.UsuarioAtual;
import br.com.goerp.compartilhado.dominio.Dinheiro;
import br.com.goerp.financeiro.dominio.FormaDePagamento;
import br.com.goerp.financeiro.dominio.Lancamento;
import br.com.goerp.financeiro.dominio.OrigemDoLancamento;
import br.com.goerp.financeiro.dominio.PedidoDeLancamento;
import br.com.goerp.financeiro.dominio.TipoDeLancamento;

/** Receita ou despesa avulsa (fora de títulos), ex.: saldo inicial, tarifa bancária, troco para o cofre. */
@CasoDeUso
public class RegistrarLancamento {

    private final LancadorFinanceiro lancador;
    private final UsuarioAtual usuarioAtual;

    public RegistrarLancamento(LancadorFinanceiro lancador, UsuarioAtual usuarioAtual) {
        this.lancador = lancador;
        this.usuarioAtual = usuarioAtual;
    }

    public Lancamento executar(Long contaId, TipoDeLancamento.Sentido sentido, Dinheiro valor, FormaDePagamento forma,
            Long categoriaId, String descricao) {
        return lancador.lancar(contaId, new PedidoDeLancamento(TipoDeLancamento.normal(sentido), valor, forma,
                categoriaId, descricao, OrigemDoLancamento.MANUAL, null, null, usuarioAtual.id()));
    }
}
