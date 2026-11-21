package br.com.goerp.vendas.aplicacao;

import java.util.List;
import java.util.Optional;

import br.com.goerp.compartilhado.aplicacao.CasoDeUso;
import br.com.goerp.compartilhado.aplicacao.UsuarioAtual;
import br.com.goerp.vendas.dominio.CaixaRepositorio;
import br.com.goerp.vendas.dominio.SituacaoDoCaixa;

@CasoDeUso
public class ConsultarCaixa {

    private final CaixaRepositorio caixas;
    private final CaixasDaEmpresa caixasDaEmpresa;
    private final UsuarioAtual usuarioAtual;

    public ConsultarCaixa(CaixaRepositorio caixas, CaixasDaEmpresa caixasDaEmpresa, UsuarioAtual usuarioAtual) {
        this.caixas = caixas;
        this.caixasDaEmpresa = caixasDaEmpresa;
        this.usuarioAtual = usuarioAtual;
    }

    /** O caixa aberto do usuário logado, se houver. */
    public Optional<ResumoDoCaixa> meuCaixaAberto() {
        return caixas.buscarAbertoDoOperador(usuarioAtual.id()).map(caixasDaEmpresa::resumir);
    }

    public ResumoDoCaixa porId(Long id) {
        return caixasDaEmpresa.resumir(caixasDaEmpresa.buscarComAcesso(id));
    }

    public List<ResumoDoCaixa> todos(SituacaoDoCaixa situacao) {
        return caixas.listar(situacao).stream().map(caixasDaEmpresa::resumir).toList();
    }
}
