package br.com.goerp.vendas.aplicacao;

import java.time.Clock;
import java.util.Comparator;
import java.util.List;

import br.com.goerp.cadastros.cliente.aplicacao.BuscarCliente;
import br.com.goerp.compartilhado.aplicacao.CasoDeUso;
import br.com.goerp.compartilhado.aplicacao.EmpresaAtual;
import br.com.goerp.compartilhado.aplicacao.PermissoesAtuais;
import br.com.goerp.compartilhado.aplicacao.UsuarioAtual;
import br.com.goerp.estoque.aplicacao.RegistrarSaida;
import br.com.goerp.estoque.dominio.OrigemDaMovimentacao;
import br.com.goerp.usuario.dominio.Permissao;
import br.com.goerp.vendas.dominio.Caixa;
import br.com.goerp.vendas.dominio.CaixaNaoAbertoException;
import br.com.goerp.vendas.dominio.CaixaRepositorio;
import br.com.goerp.vendas.dominio.ConfiguracaoDeVendasRepositorio;
import br.com.goerp.vendas.dominio.ItemDeVenda;
import br.com.goerp.vendas.dominio.NumeracaoDeVendas;
import br.com.goerp.vendas.dominio.PoliticaDeDesconto;
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
    private final ItensDoCadastro itensDoCadastro;
    private final BuscarCliente buscarCliente;
    private final RegistrarSaida registrarSaida;
    private final UsuarioAtual usuarioAtual;
    private final EmpresaAtual empresaAtual;
    private final PermissoesAtuais permissoes;
    private final Clock relogio;

    public RegistrarVenda(CaixaRepositorio caixas, VendaRepositorio vendas, NumeracaoDeVendas numeracao,
            ConfiguracaoDeVendasRepositorio configuracao, ItensDoCadastro itensDoCadastro, BuscarCliente buscarCliente,
            RegistrarSaida registrarSaida, UsuarioAtual usuarioAtual, EmpresaAtual empresaAtual,
            PermissoesAtuais permissoes, Clock relogio) {
        this.caixas = caixas;
        this.vendas = vendas;
        this.numeracao = numeracao;
        this.configuracao = configuracao;
        this.itensDoCadastro = itensDoCadastro;
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
        List<ItemDeVenda> itens = itensDoCadastro.montar(solicitacao.itens());
        var politica = new PoliticaDeDesconto(configuracao.descontoMaximoPercentual(),
                permissoes.possui(Permissao.VENDA_DESCONTO.name()));

        Venda venda = Venda.registrar(new Venda.DadosDaVenda(empresaAtual.id(), caixa.id(), usuarioAtual.id(),
                solicitacao.clienteId(), solicitacao.cpfNaNota(), itens, solicitacao.descontoNaVenda(),
                solicitacao.pagamentos()), politica, relogio.instant());
        Venda gravada = vendas.salvar(venda.comNumero(numeracao.proximo(empresaAtual.id())));
        baixarEstoque(gravada);
        return gravada;
    }

    /**
     * Baixa sempre na mesma ordem (por produto): duas vendas simultâneas com os mesmos produtos
     * travam os saldos na mesma sequência e nunca ficam esperando uma pela outra (deadlock).
     * Produtos que não controlam estoque (serviços) não geram movimentação.
     */
    private void baixarEstoque(Venda venda) {
        venda.itens().stream()
                .sorted(Comparator.comparing(ItemDeVenda::produtoId))
                .filter(item -> itensDoCadastro.controlaEstoque(item.produtoId()))
                .forEach(item -> registrarSaida.executar(item.produtoId(), item.quantidade(),
                        OrigemDaMovimentacao.VENDA, venda.id(), "Venda nº " + venda.numero()));
    }
}
