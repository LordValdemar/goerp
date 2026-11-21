package br.com.goerp.vendas.aplicacao;

import java.time.Clock;

import br.com.goerp.compartilhado.aplicacao.CasoDeUso;
import br.com.goerp.compartilhado.aplicacao.EmpresaAtual;
import br.com.goerp.compartilhado.aplicacao.UsuarioAtual;
import br.com.goerp.compartilhado.dominio.Dinheiro;
import br.com.goerp.vendas.dominio.Caixa;
import br.com.goerp.vendas.dominio.CaixaJaAbertoException;
import br.com.goerp.vendas.dominio.CaixaRepositorio;

/** Abre o caixa do usuário logado. Cada operador tem no máximo um caixa aberto. */
@CasoDeUso
public class AbrirCaixa {

    private final CaixaRepositorio caixas;
    private final CaixasDaEmpresa caixasDaEmpresa;
    private final UsuarioAtual usuarioAtual;
    private final EmpresaAtual empresaAtual;
    private final Clock relogio;

    public AbrirCaixa(CaixaRepositorio caixas, CaixasDaEmpresa caixasDaEmpresa, UsuarioAtual usuarioAtual,
            EmpresaAtual empresaAtual, Clock relogio) {
        this.caixas = caixas;
        this.caixasDaEmpresa = caixasDaEmpresa;
        this.usuarioAtual = usuarioAtual;
        this.empresaAtual = empresaAtual;
        this.relogio = relogio;
    }

    public ResumoDoCaixa executar(Dinheiro trocoInicial) {
        if (caixas.buscarAbertoDoOperador(usuarioAtual.id()).isPresent()) {
            throw new CaixaJaAbertoException();
        }
        Caixa caixa = caixas.salvar(Caixa.abrir(empresaAtual.id(), usuarioAtual.id(), trocoInicial, relogio.instant()));
        return caixasDaEmpresa.resumir(caixa);
    }
}
