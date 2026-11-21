package br.com.goerp.estoque.aplicacao;

import java.util.List;

import br.com.goerp.compartilhado.aplicacao.CasoDeUso;

/**
 * Recalcula cada saldo a partir das movimentações e aponta divergências. Com o saldo só mudando
 * junto de uma movimentação, a lista deve vir sempre vazia; se não vier, algo alterou o banco por fora.
 */
@CasoDeUso
public class ConferirSaldos {

    private final ConsultasDeEstoque consultas;

    public ConferirSaldos(ConsultasDeEstoque consultas) {
        this.consultas = consultas;
    }

    public List<DivergenciaDeSaldo> executar() {
        return consultas.divergencias();
    }
}
