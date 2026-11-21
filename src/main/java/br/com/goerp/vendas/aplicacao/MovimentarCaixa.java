package br.com.goerp.vendas.aplicacao;

import java.time.Clock;

import br.com.goerp.compartilhado.aplicacao.CasoDeUso;
import br.com.goerp.compartilhado.aplicacao.UsuarioAtual;
import br.com.goerp.compartilhado.dominio.Dinheiro;
import br.com.goerp.vendas.dominio.Caixa;
import br.com.goerp.vendas.dominio.CaixaRepositorio;
import br.com.goerp.vendas.dominio.TipoDeMovimentoDeCaixa;

/** Suprimento (reforço de troco) ou sangria (retirada de dinheiro, com motivo). */
@CasoDeUso
public class MovimentarCaixa {

    private final CaixaRepositorio caixas;
    private final CaixasDaEmpresa caixasDaEmpresa;
    private final UsuarioAtual usuarioAtual;
    private final Clock relogio;

    public MovimentarCaixa(CaixaRepositorio caixas, CaixasDaEmpresa caixasDaEmpresa, UsuarioAtual usuarioAtual,
            Clock relogio) {
        this.caixas = caixas;
        this.caixasDaEmpresa = caixasDaEmpresa;
        this.usuarioAtual = usuarioAtual;
        this.relogio = relogio;
    }

    public ResumoDoCaixa executar(Long caixaId, TipoDeMovimentoDeCaixa tipo, Dinheiro valor, String motivo) {
        Caixa caixa = caixasDaEmpresa.buscarComAcesso(caixaId);
        if (tipo == TipoDeMovimentoDeCaixa.SUPRIMENTO) {
            caixa.registrarSuprimento(valor, motivo, usuarioAtual.id(), relogio.instant());
        } else {
            caixa.registrarSangria(valor, motivo, usuarioAtual.id(), relogio.instant(),
                    caixasDaEmpresa.recebidoPelasVendas(caixa));
        }
        return caixasDaEmpresa.resumir(caixas.salvar(caixa));
    }
}
