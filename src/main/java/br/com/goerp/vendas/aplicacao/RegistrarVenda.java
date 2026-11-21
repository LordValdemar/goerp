package br.com.goerp.vendas.aplicacao;

import java.time.Clock;
import java.util.Comparator;
import java.util.List;

import br.com.goerp.cadastros.cliente.aplicacao.BuscarCliente;
import br.com.goerp.cadastros.produto.aplicacao.BuscarProduto;
import br.com.goerp.cadastros.produto.aplicacao.BuscarProdutoPorCodigoDeBarras;
import br.com.goerp.cadastros.produto.aplicacao.ProdutoDetalhado;
import br.com.goerp.compartilhado.aplicacao.CasoDeUso;
import br.com.goerp.compartilhado.aplicacao.EmpresaAtual;
import br.com.goerp.compartilhado.aplicacao.PermissoesAtuais;
import br.com.goerp.compartilhado.aplicacao.UsuarioAtual;
import br.com.goerp.estoque.aplicacao.RegistrarSaida;
import br.com.goerp.estoque.dominio.OrigemDaMovimentacao;
import br.com.goerp.usuario.dominio.Permissao;
import br.com.goerp.vendas.aplicacao.SolicitacaoDeVenda.ItemSolicitado;
import br.com.goerp.vendas.dominio.Caixa;
import br.com.goerp.vendas.dominio.CaixaNaoAbertoException;
import br.com.goerp.vendas.dominio.CaixaRepositorio;
import br.com.goerp.vendas.dominio.ConfiguracaoDeVendasRepositorio;
import br.com.goerp.vendas.dominio.ItemDeVenda;
import br.com.goerp.vendas.dominio.ItemSemProdutoException;
import br.com.goerp.vendas.dominio.NumeracaoDeVendas;
import br.com.goerp.vendas.dominio.PoliticaDeDesconto;
import br.com.goerp.vendas.dominio.ProdutoInativoNaVendaException;
import br.com.goerp.vendas.dominio.QuantidadeFracionadaNaVendaException;
import br.com.goerp.vendas.dominio.Venda;
import br.com.goerp.vendas.dominio.VendaRepositorio;

/**
 * Venda no PDV, numa única transação: confere caixa, produtos, descontos e pagamentos, numera,
 * grava e baixa o estoque de cada item. Se qualquer item não tiver estoque, nada é gravado.
 */
@CasoDeUso
public class RegistrarVenda {

    private final CaixaRepositorio caixas;
    private final VendaRepositorio vendas;
    private final NumeracaoDeVendas numeracao;
    private final ConfiguracaoDeVendasRepositorio configuracao;
    private final BuscarProduto buscarProduto;
    private final BuscarProdutoPorCodigoDeBarras buscarPorCodigoDeBarras;
    private final BuscarCliente buscarCliente;
    private final RegistrarSaida registrarSaida;
    private final UsuarioAtual usuarioAtual;
    private final EmpresaAtual empresaAtual;
    private final PermissoesAtuais permissoes;
    private final Clock relogio;

    public RegistrarVenda(CaixaRepositorio caixas, VendaRepositorio vendas, NumeracaoDeVendas numeracao,
            ConfiguracaoDeVendasRepositorio configuracao, BuscarProduto buscarProduto,
            BuscarProdutoPorCodigoDeBarras buscarPorCodigoDeBarras, BuscarCliente buscarCliente,
            RegistrarSaida registrarSaida, UsuarioAtual usuarioAtual, EmpresaAtual empresaAtual,
            PermissoesAtuais permissoes, Clock relogio) {
        this.caixas = caixas;
        this.vendas = vendas;
        this.numeracao = numeracao;
        this.configuracao = configuracao;
        this.buscarProduto = buscarProduto;
        this.buscarPorCodigoDeBarras = buscarPorCodigoDeBarras;
        this.buscarCliente = buscarCliente;
        this.registrarSaida = registrarSaida;
        this.usuarioAtual = usuarioAtual;
        this.empresaAtual = empresaAtual;
        this.permissoes = permissoes;
        this.relogio = relogio;
    }

    public Venda executar(SolicitacaoDeVenda solicitacao) {
        Caixa caixa = caixas.buscarAbertoDoOperador(usuarioAtual.id()).orElseThrow(CaixaNaoAbertoException::new);
        if (solicitacao.clienteId() != null) {
            buscarCliente.executar(solicitacao.clienteId());
        }
        List<ItemDeVenda> itens = solicitacao.itens().stream().map(this::itemDoCadastro).toList();
        var politica = new PoliticaDeDesconto(configuracao.descontoMaximoPercentual(),
                permissoes.possui(Permissao.VENDA_DESCONTO.name()));

        Venda venda = Venda.registrar(new Venda.DadosDaVenda(empresaAtual.id(), caixa.id(), usuarioAtual.id(),
                solicitacao.clienteId(), solicitacao.cpfNaNota(), itens, solicitacao.descontoNaVenda(),
                solicitacao.pagamentos()), politica, relogio.instant());
        Venda gravada = vendas.salvar(venda.comNumero(numeracao.proximo(empresaAtual.id())));
        baixarEstoque(gravada);
        return gravada;
    }

    /** Preço, código e descrição vêm do cadastro: o PDV não escolhe o preço. */
    private ItemDeVenda itemDoCadastro(ItemSolicitado solicitado) {
        ProdutoDetalhado detalhado = localizar(solicitado);
        var produto = detalhado.produto();
        if (!produto.ativo()) {
            throw new ProdutoInativoNaVendaException();
        }
        if (!detalhado.unidade().fracionavel() && !solicitado.quantidade().inteira()) {
            throw new QuantidadeFracionadaNaVendaException();
        }
        return new ItemDeVenda(produto.id(), produto.codigo(), produto.dados().descricao(), solicitado.quantidade(),
                produto.dados().precoDeVenda(), solicitado.desconto());
    }

    private ProdutoDetalhado localizar(ItemSolicitado solicitado) {
        if (solicitado.produtoId() != null) {
            return buscarProduto.executar(solicitado.produtoId());
        }
        if (solicitado.codigoDeBarras() != null && !solicitado.codigoDeBarras().isBlank()) {
            return buscarPorCodigoDeBarras.executar(solicitado.codigoDeBarras());
        }
        throw new ItemSemProdutoException();
    }

    /**
     * Baixa sempre na mesma ordem (por produto): duas vendas simultâneas com os mesmos produtos
     * travam os saldos na mesma sequência e nunca ficam esperando uma pela outra (deadlock).
     * Produtos que não controlam estoque (serviços) não geram movimentação.
     */
    private void baixarEstoque(Venda venda) {
        venda.itens().stream()
                .sorted(Comparator.comparing(ItemDeVenda::produtoId))
                .filter(item -> buscarProduto.executar(item.produtoId()).produto().dados().controlaEstoque())
                .forEach(item -> registrarSaida.executar(item.produtoId(), item.quantidade(),
                        OrigemDaMovimentacao.VENDA, venda.id(), "Venda nº " + venda.numero()));
    }
}
