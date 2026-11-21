package br.com.goerp.financeiro.aplicacao;

import java.time.Clock;

import br.com.goerp.compartilhado.aplicacao.CasoDeUso;
import br.com.goerp.compartilhado.aplicacao.UsuarioAtual;
import br.com.goerp.compartilhado.dominio.Dinheiro;
import br.com.goerp.financeiro.dominio.Baixa;
import br.com.goerp.financeiro.dominio.FormaDePagamento;
import br.com.goerp.financeiro.dominio.NaturezaDoTitulo;
import br.com.goerp.financeiro.dominio.OrigemDoLancamento;
import br.com.goerp.financeiro.dominio.PedidoDeBaixa;
import br.com.goerp.financeiro.dominio.PedidoDeLancamento;
import br.com.goerp.financeiro.dominio.TipoDeLancamento;
import br.com.goerp.financeiro.dominio.Titulo;
import br.com.goerp.financeiro.dominio.TituloRepositorio;

/**
 * Registra um pagamento (ou recebimento) e o lançamento correspondente na conta escolhida, com a
 * forma usada, numa transação só. O título fica travado: dois pagamentos simultâneos não pagam a mais.
 */
@CasoDeUso
public class BaixarTitulo {

    private final TituloRepositorio titulos;
    private final TitulosDaEmpresa titulosDaEmpresa;
    private final BuscarContaFinanceira buscarConta;
    private final LancadorFinanceiro lancador;
    private final UsuarioAtual usuarioAtual;
    private final Clock relogio;

    public BaixarTitulo(TituloRepositorio titulos, TitulosDaEmpresa titulosDaEmpresa, BuscarContaFinanceira buscarConta,
            LancadorFinanceiro lancador, UsuarioAtual usuarioAtual, Clock relogio) {
        this.titulos = titulos;
        this.titulosDaEmpresa = titulosDaEmpresa;
        this.buscarConta = buscarConta;
        this.lancador = lancador;
        this.usuarioAtual = usuarioAtual;
        this.relogio = relogio;
    }

    public TituloConsultado executar(NaturezaDoTitulo natureza, Long id, Long contaId, FormaDePagamento forma,
            Dinheiro valorPago, Dinheiro juros, Dinheiro multa, Dinheiro desconto) {
        Titulo titulo = titulosDaEmpresa.travar(natureza, id);
        // conta de outra empresa é inexistente (404), antes de gravar o pagamento que apontaria para ela
        buscarConta.executar(contaId);
        PedidoDeBaixa pedido = new PedidoDeBaixa(contaId, forma, valorPago, juros, multa, desconto, usuarioAtual.id());
        int numero = titulo.baixar(pedido, relogio.instant()).numero();
        Titulo salvo = titulos.salvar(titulo);
        Baixa baixa = salvo.baixa(numero);
        lancador.lancar(contaId, new PedidoDeLancamento(TipoDeLancamento.normal(natureza.sentidoDaBaixa()),
                baixa.valorPago(), baixa.forma(), salvo.categoriaId(), HistoricoDaBaixa.de(salvo),
                OrigemDoLancamento.BAIXA_DE_TITULO, baixa.id(), null, usuarioAtual.id()));
        return titulosDaEmpresa.consultado(salvo);
    }
}
