package br.com.goerp.vendas.aplicacao;

import java.time.Clock;
import java.util.Map;

import br.com.goerp.compartilhado.aplicacao.CasoDeUso;
import br.com.goerp.compartilhado.aplicacao.UsuarioAtual;
import br.com.goerp.compartilhado.dominio.Dinheiro;
import br.com.goerp.vendas.dominio.Caixa;
import br.com.goerp.vendas.dominio.CaixaRepositorio;
import br.com.goerp.financeiro.dominio.FormaDePagamento;

/**
 * Fecha o caixa com a contagem do operador. O operador fecha o próprio; o de outro só com
 * permissão de gerente. Sobras e faltas ficam registradas na conferência.
 */
@CasoDeUso
public class FecharCaixa {

    private final CaixaRepositorio caixas;
    private final CaixasDaEmpresa caixasDaEmpresa;
    private final UsuarioAtual usuarioAtual;
    private final Clock relogio;

    public FecharCaixa(CaixaRepositorio caixas, CaixasDaEmpresa caixasDaEmpresa, UsuarioAtual usuarioAtual,
            Clock relogio) {
        this.caixas = caixas;
        this.caixasDaEmpresa = caixasDaEmpresa;
        this.usuarioAtual = usuarioAtual;
        this.relogio = relogio;
    }

    public ResumoDoCaixa executar(Long caixaId, Map<FormaDePagamento, Dinheiro> contado) {
        Caixa caixa = caixasDaEmpresa.buscarComAcesso(caixaId);
        caixa.fechar(contado, caixasDaEmpresa.recebidoPelasVendas(caixa), usuarioAtual.id(), relogio.instant());
        return caixasDaEmpresa.resumir(caixas.salvar(caixa));
    }
}
