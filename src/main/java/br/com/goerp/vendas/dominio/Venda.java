package br.com.goerp.vendas.dominio;

import static br.com.goerp.compartilhado.dominio.Textos.obrigatorio;

import java.time.Instant;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import br.com.goerp.compartilhado.dominio.Cpf;
import br.com.goerp.compartilhado.dominio.Dinheiro;

/**
 * Venda do PDV, registrada já finalizada: itens e pagamentos chegam juntos e são validados de uma vez.
 *
 * <p>Regras de valores: total = itens - descontos; os pagamentos precisam cobrir o total e só o
 * dinheiro pode passar dele (o excedente é troco).
 */
public class Venda {

    private final Long id;
    private final Long numero;
    private final Long empresaId;
    private final Long caixaId;
    private final Long operadorId;
    private final Long clienteId;
    private final Cpf cpfNaNota;
    private final List<ItemDeVenda> itens;
    private final Dinheiro descontoNaVenda;
    private final List<Pagamento> pagamentos;
    private final Instant momento;
    private SituacaoDaVenda situacao;
    private Instant canceladaEm;
    private String motivoDoCancelamento;
    private Long canceladaPor;

    private Venda(Long id, Long numero, DadosDaVenda dados, Instant momento, SituacaoDaVenda situacao,
            Instant canceladaEm, String motivoDoCancelamento, Long canceladaPor) {
        this.id = id;
        this.numero = numero;
        this.empresaId = Objects.requireNonNull(dados.empresaId(), "empresaId");
        this.caixaId = Objects.requireNonNull(dados.caixaId(), "caixaId");
        this.operadorId = Objects.requireNonNull(dados.operadorId(), "operadorId");
        this.clienteId = dados.clienteId();
        this.cpfNaNota = dados.cpfNaNota();
        this.itens = List.copyOf(dados.itens());
        this.descontoNaVenda = Objects.requireNonNullElse(dados.descontoNaVenda(), Dinheiro.ZERO);
        this.pagamentos = List.copyOf(dados.pagamentos());
        this.momento = Objects.requireNonNull(momento, "momento");
        this.situacao = situacao;
        this.canceladaEm = canceladaEm;
        this.motivoDoCancelamento = motivoDoCancelamento;
        this.canceladaPor = canceladaPor;
    }

    /** Tudo o que define a venda (sem número, situação e datas). */
    public record DadosDaVenda(
            Long empresaId,
            Long caixaId,
            Long operadorId,
            Long clienteId,
            Cpf cpfNaNota,
            List<ItemDeVenda> itens,
            Dinheiro descontoNaVenda,
            List<Pagamento> pagamentos) {
    }

    public static Venda registrar(DadosDaVenda dados, PoliticaDeDesconto politica, Instant agora) {
        Venda venda = new Venda(null, null, dados, agora, SituacaoDaVenda.FINALIZADA, null, null, null);
        venda.validar(politica);
        return venda;
    }

    public static Venda reconstituir(Long id, Long numero, DadosDaVenda dados, Instant momento,
            SituacaoDaVenda situacao, Instant canceladaEm, String motivoDoCancelamento, Long canceladaPor) {
        return new Venda(id, numero, dados, momento, situacao, canceladaEm, motivoDoCancelamento, canceladaPor);
    }

    private void validar(PoliticaDeDesconto politica) {
        if (itens.isEmpty()) {
            throw new VendaSemItensException();
        }
        descontoNaVenda.exigirNaoNegativo("descontoNaVenda");
        Dinheiro desconto = descontoTotal();
        if (desconto.compareTo(Dinheiro.ZERO) > 0) {
            if (!politica.operadorPodeDescontar()) {
                throw new DescontoNaoPermitidoException();
            }
            if (desconto.valor().compareTo(politica.limiteSobre(bruto().valor())) > 0) {
                throw new DescontoAcimaDoLimiteException(politica.percentualMaximo());
            }
        }
        validarPagamentos();
    }

    private void validarPagamentos() {
        Dinheiro pago = totalPago();
        if (pago.compareTo(total()) < 0) {
            throw new PagamentoInsuficienteException();
        }
        if (troco().compareTo(pagoEm(FormaDePagamento.DINHEIRO)) > 0) {
            throw new PagamentoAcimaDoTotalException();
        }
    }

    /** Cancela a venda; quem chama devolve a mercadoria ao estoque. */
    public void cancelar(String motivo, Long usuarioId, Instant agora) {
        if (situacao == SituacaoDaVenda.CANCELADA) {
            throw new VendaJaCanceladaException();
        }
        this.motivoDoCancelamento = obrigatorio(motivo, "motivo");
        this.canceladaPor = Objects.requireNonNull(usuarioId, "usuarioId");
        this.situacao = SituacaoDaVenda.CANCELADA;
        this.canceladaEm = agora;
    }

    public Dinheiro bruto() {
        return itens.stream().map(ItemDeVenda::bruto).reduce(Dinheiro.ZERO, Dinheiro::somar);
    }

    public Dinheiro descontoTotal() {
        return itens.stream().map(ItemDeVenda::desconto).reduce(descontoNaVenda, Dinheiro::somar);
    }

    public Dinheiro total() {
        return bruto().subtrair(descontoTotal());
    }

    public Dinheiro totalPago() {
        return pagamentos.stream().map(Pagamento::valor).reduce(Dinheiro.ZERO, Dinheiro::somar);
    }

    public Dinheiro troco() {
        return totalPago().subtrair(total());
    }

    /**
     * Quanto efetivamente entrou no caixa por forma de pagamento: o dinheiro já sem o troco.
     * É o que o fechamento do caixa espera encontrar.
     */
    public Map<FormaDePagamento, Dinheiro> recebidoPorForma() {
        Map<FormaDePagamento, Dinheiro> recebido = new EnumMap<>(FormaDePagamento.class);
        pagamentos.forEach(pagamento -> recebido.merge(pagamento.forma(), pagamento.valor(), Dinheiro::somar));
        recebido.computeIfPresent(FormaDePagamento.DINHEIRO, (forma, valor) -> valor.subtrair(troco()));
        return recebido;
    }

    private Dinheiro pagoEm(FormaDePagamento forma) {
        return pagamentos.stream().filter(pagamento -> pagamento.forma() == forma).map(Pagamento::valor)
                .reduce(Dinheiro.ZERO, Dinheiro::somar);
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

    public Long caixaId() {
        return caixaId;
    }

    public Long operadorId() {
        return operadorId;
    }

    public Long clienteId() {
        return clienteId;
    }

    public Cpf cpfNaNota() {
        return cpfNaNota;
    }

    public List<ItemDeVenda> itens() {
        return itens;
    }

    public Dinheiro descontoNaVenda() {
        return descontoNaVenda;
    }

    public List<Pagamento> pagamentos() {
        return pagamentos;
    }

    public Instant momento() {
        return momento;
    }

    public SituacaoDaVenda situacao() {
        return situacao;
    }

    public Instant canceladaEm() {
        return canceladaEm;
    }

    public String motivoDoCancelamento() {
        return motivoDoCancelamento;
    }

    public Long canceladaPor() {
        return canceladaPor;
    }

    public DadosDaVenda dados() {
        return new DadosDaVenda(empresaId, caixaId, operadorId, clienteId, cpfNaNota, itens, descontoNaVenda,
                pagamentos);
    }
}
