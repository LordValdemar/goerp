package br.com.goerp.vendas.aplicacao.pedido;

import br.com.goerp.compartilhado.aplicacao.PermissoesAtuais;
import br.com.goerp.compartilhado.aplicacao.ServicoDeAplicacao;
import br.com.goerp.compartilhado.dominio.excecao.RecursoNaoEncontradoException;
import br.com.goerp.usuario.dominio.Permissao;
import br.com.goerp.vendas.aplicacao.ItensDoCadastro;
import br.com.goerp.vendas.dominio.ConfiguracaoDeVendasRepositorio;
import br.com.goerp.vendas.dominio.PedidoDeVenda;
import br.com.goerp.vendas.dominio.PedidoDeVendaRepositorio;
import br.com.goerp.vendas.dominio.PoliticaDeDesconto;

/** O que os casos de uso de pedido compartilham: busca, conteúdo a partir do cadastro e política de desconto. */
@ServicoDeAplicacao
public class PedidosDaEmpresa {

    private final PedidoDeVendaRepositorio pedidos;
    private final ItensDoCadastro itensDoCadastro;
    private final ConfiguracaoDeVendasRepositorio configuracao;
    private final PermissoesAtuais permissoes;

    public PedidosDaEmpresa(PedidoDeVendaRepositorio pedidos, ItensDoCadastro itensDoCadastro,
            ConfiguracaoDeVendasRepositorio configuracao, PermissoesAtuais permissoes) {
        this.pedidos = pedidos;
        this.itensDoCadastro = itensDoCadastro;
        this.configuracao = configuracao;
        this.permissoes = permissoes;
    }

    public PedidoDeVenda buscar(Long id) {
        return pedidos.buscarPorId(id).orElseThrow(() -> new RecursoNaoEncontradoException("Pedido de venda", id));
    }

    public PedidoDeVenda.Conteudo conteudo(DadosDoPedido dados) {
        return new PedidoDeVenda.Conteudo(itensDoCadastro.montar(dados.itens()), dados.desconto(), dados.validade(),
                dados.observacoes());
    }

    public PoliticaDeDesconto politicaDeDesconto() {
        return new PoliticaDeDesconto(configuracao.descontoMaximoPercentual(),
                permissoes.possui(Permissao.VENDA_DESCONTO.name()));
    }

    public ItensDoCadastro itensDoCadastro() {
        return itensDoCadastro;
    }
}
