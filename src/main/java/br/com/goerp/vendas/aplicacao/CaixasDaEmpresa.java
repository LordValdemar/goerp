package br.com.goerp.vendas.aplicacao;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.Map;

import br.com.goerp.compartilhado.aplicacao.PermissoesAtuais;
import br.com.goerp.compartilhado.aplicacao.ServicoDeAplicacao;
import br.com.goerp.compartilhado.aplicacao.UsuarioAtual;
import br.com.goerp.compartilhado.dominio.Dinheiro;
import br.com.goerp.compartilhado.dominio.excecao.OperacaoNaoPermitidaException;
import br.com.goerp.compartilhado.dominio.excecao.RecursoNaoEncontradoException;
import br.com.goerp.usuario.dominio.Permissao;
import br.com.goerp.vendas.dominio.Caixa;
import br.com.goerp.vendas.dominio.CaixaRepositorio;
import br.com.goerp.vendas.dominio.FormaDePagamento;
import br.com.goerp.vendas.dominio.VendasDoCaixa;

/** Busca de caixas com a regra de acesso: cada operador só mexe no próprio, salvo o gerente. */
@ServicoDeAplicacao
public class CaixasDaEmpresa {

    private final CaixaRepositorio caixas;
    private final VendasDoCaixa vendasDoCaixa;
    private final UsuarioAtual usuarioAtual;
    private final PermissoesAtuais permissoes;

    public CaixasDaEmpresa(CaixaRepositorio caixas, VendasDoCaixa vendasDoCaixa, UsuarioAtual usuarioAtual,
            PermissoesAtuais permissoes) {
        this.caixas = caixas;
        this.vendasDoCaixa = vendasDoCaixa;
        this.usuarioAtual = usuarioAtual;
        this.permissoes = permissoes;
    }

    public Caixa buscarComAcesso(Long caixaId) {
        Caixa caixa = caixas.buscarPorId(caixaId).orElseThrow(() -> new RecursoNaoEncontradoException("Caixa", caixaId));
        boolean doProprioOperador = caixa.operadorId().equals(usuarioAtual.id());
        if (!doProprioOperador && !permissoes.possui(Permissao.CAIXA_GERENCIAR.name())) {
            throw new OperacaoNaoPermitidaException("Este caixa é de outro operador");
        }
        return caixa;
    }

    public Map<FormaDePagamento, Dinheiro> recebidoPelasVendas(Caixa caixa) {
        return vendasDoCaixa.recebidoPorForma(caixa.id());
    }

    public ResumoDoCaixa resumir(Caixa caixa) {
        Map<FormaDePagamento, Dinheiro> recebido = recebidoPelasVendas(caixa);
        Map<FormaDePagamento, Dinheiro> esperado = new EnumMap<>(FormaDePagamento.class);
        Arrays.stream(FormaDePagamento.values()).forEach(forma -> esperado.put(forma, caixa.esperadoEm(forma, recebido)));
        return new ResumoDoCaixa(caixa, esperado);
    }
}
