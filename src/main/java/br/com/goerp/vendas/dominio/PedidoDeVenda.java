package br.com.goerp.vendas.dominio;

import static br.com.goerp.compartilhado.dominio.Textos.obrigatorio;
import static br.com.goerp.compartilhado.dominio.Textos.opcional;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

import br.com.goerp.compartilhado.dominio.Dinheiro;
import br.com.goerp.compartilhado.dominio.excecao.ValorInvalidoException;

/**
 * Pedido de venda para cliente cadastrado (venda com entrega, venda a empresas...).
 * Nasce como orçamento, com validade; enquanto orçamento, pode ser alterado (os preços são
 * recopiados do cadastro). Confirmado, fica congelado até o faturamento, que baixa o estoque.
 */
public class PedidoDeVenda {

    private final Long id;
    private final Long numero;
    private final Long empresaId;
    private final Long clienteId;
    private final Long vendedorId;
    private final Instant criadoEm;
    private final Long versao;
    private List<ItemDeVenda> itens;
    private Dinheiro desconto;
    private LocalDate validade;
    private String observacoes;
    private SituacaoDoPedido situacao;
    private Instant confirmadoEm;
    private Instant faturadoEm;
    private Instant canceladoEm;
    private String motivoDoCancelamento;

    /** Conteúdo alterável enquanto orçamento. */
    public record Conteudo(List<ItemDeVenda> itens, Dinheiro desconto, LocalDate validade, String observacoes) {
    }

    /** Datas e situação, para reconstituir do banco. */
    public record Andamento(SituacaoDoPedido situacao, Instant confirmadoEm, Instant faturadoEm, Instant canceladoEm,
            String motivoDoCancelamento) {
    }

    private PedidoDeVenda(Long id, Long numero, Long empresaId, Long clienteId, Long vendedorId, Instant criadoEm,
            Conteudo conteudo, Andamento andamento, Long versao) {
        this.id = id;
        this.numero = numero;
        this.empresaId = Objects.requireNonNull(empresaId, "empresaId");
        this.clienteId = Objects.requireNonNull(clienteId, "clienteId");
        this.vendedorId = Objects.requireNonNull(vendedorId, "vendedorId");
        this.criadoEm = Objects.requireNonNull(criadoEm, "criadoEm");
        this.versao = versao;
        definirConteudo(conteudo);
        this.situacao = andamento.situacao();
        this.confirmadoEm = andamento.confirmadoEm();
        this.faturadoEm = andamento.faturadoEm();
        this.canceladoEm = andamento.canceladoEm();
        this.motivoDoCancelamento = andamento.motivoDoCancelamento();
    }

    public static PedidoDeVenda orcar(Long empresaId, Long numero, Long clienteId, Long vendedorId, Conteudo conteudo,
            PoliticaDeDesconto politica, LocalDate hoje, Instant agora) {
        var pedido = new PedidoDeVenda(null, numero, empresaId, clienteId, vendedorId, agora, conteudo,
                new Andamento(SituacaoDoPedido.ORCAMENTO, null, null, null, null), null);
        pedido.validar(politica, hoje);
        return pedido;
    }

    public static PedidoDeVenda reconstituir(Long id, Long numero, Long empresaId, Long clienteId, Long vendedorId,
            Instant criadoEm, Conteudo conteudo, Andamento andamento, Long versao) {
        return new PedidoDeVenda(id, numero, empresaId, clienteId, vendedorId, criadoEm, conteudo, andamento, versao);
    }

    public void alterar(Conteudo conteudo, PoliticaDeDesconto politica, LocalDate hoje) {
        exigir(SituacaoDoPedido.ORCAMENTO, "alterado");
        definirConteudo(conteudo);
        validar(politica, hoje);
    }

    public void confirmar(LocalDate hoje, Instant agora) {
        exigir(SituacaoDoPedido.ORCAMENTO, "confirmado");
        if (hoje.isAfter(validade)) {
            throw new OrcamentoVencidoException(validade);
        }
        situacao = SituacaoDoPedido.CONFIRMADO;
        confirmadoEm = agora;
    }

    /** Quem fatura baixa o estoque, na mesma transação. */
    public void faturar(Instant agora) {
        exigir(SituacaoDoPedido.CONFIRMADO, "faturado");
        situacao = SituacaoDoPedido.FATURADO;
        faturadoEm = agora;
    }

    public void cancelar(String motivo, Instant agora) {
        if (situacao != SituacaoDoPedido.ORCAMENTO && situacao != SituacaoDoPedido.CONFIRMADO) {
            throw new TransicaoDePedidoInvalidaException(situacao, "cancelado");
        }
        motivoDoCancelamento = obrigatorio(motivo, "motivo");
        situacao = SituacaoDoPedido.CANCELADO;
        canceladoEm = agora;
    }

    public Dinheiro bruto() {
        return itens.stream().map(ItemDeVenda::bruto).reduce(Dinheiro.ZERO, Dinheiro::somar);
    }

    public Dinheiro descontoTotal() {
        return itens.stream().map(ItemDeVenda::desconto).reduce(desconto, Dinheiro::somar);
    }

    public Dinheiro total() {
        return bruto().subtrair(descontoTotal());
    }

    private void definirConteudo(Conteudo conteudo) {
        this.itens = List.copyOf(conteudo.itens());
        this.desconto = Objects.requireNonNullElse(conteudo.desconto(), Dinheiro.ZERO).exigirNaoNegativo("desconto");
        this.validade = Objects.requireNonNull(conteudo.validade(), "validade");
        this.observacoes = opcional(conteudo.observacoes());
    }

    private void validar(PoliticaDeDesconto politica, LocalDate hoje) {
        if (itens.isEmpty()) {
            throw new VendaSemItensException();
        }
        if (validade.isBefore(hoje)) {
            throw new ValorInvalidoException("A validade do orçamento não pode ser anterior a hoje");
        }
        Dinheiro descontos = descontoTotal();
        if (descontos.compareTo(Dinheiro.ZERO) > 0) {
            if (!politica.operadorPodeDescontar()) {
                throw new DescontoNaoPermitidoException();
            }
            if (descontos.valor().compareTo(politica.limiteSobre(bruto().valor())) > 0) {
                throw new DescontoAcimaDoLimiteException(politica.percentualMaximo());
            }
        }
    }

    private void exigir(SituacaoDoPedido esperada, String acao) {
        if (situacao != esperada) {
            throw new TransicaoDePedidoInvalidaException(situacao, acao);
        }
    }

    public Long id() {
        return id;
    }

    public Long numero() {
        return numero;
    }

    public Long empresaId() {
        return empresaId;
    }

    public Long clienteId() {
        return clienteId;
    }

    public Long vendedorId() {
        return vendedorId;
    }

    public Instant criadoEm() {
        return criadoEm;
    }

    public Long versao() {
        return versao;
    }

    public Conteudo conteudo() {
        return new Conteudo(itens, desconto, validade, observacoes);
    }

    public Andamento andamento() {
        return new Andamento(situacao, confirmadoEm, faturadoEm, canceladoEm, motivoDoCancelamento);
    }

    public List<ItemDeVenda> itens() {
        return itens;
    }

    public SituacaoDoPedido situacao() {
        return situacao;
    }
}
