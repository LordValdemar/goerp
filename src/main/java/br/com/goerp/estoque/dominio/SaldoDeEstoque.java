package br.com.goerp.estoque.dominio;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.Objects;

import br.com.goerp.cadastros.produto.dominio.CustoUnitario;
import br.com.goerp.compartilhado.dominio.Quantidade;

/**
 * Saldo e custo médio de um produto. Só muda registrando uma {@link Movimentacao}: é a única
 * porta de entrada, e cada movimentação guarda o saldo resultante.
 *
 * <p>Custo médio ponderado: cada entrada recalcula
 * {@code (saldo x custoMedio + quantidade x custo) / (saldo + quantidade)}; saídas levam o custo
 * médio do momento (base do custo da mercadoria vendida) e não o alteram.
 */
public class SaldoDeEstoque {

    private static final RoundingMode ARREDONDAMENTO = RoundingMode.HALF_EVEN;

    private final Long id;
    private final Long empresaId;
    private final Long produtoId;
    private Quantidade quantidade;
    private CustoUnitario custoMedio;

    private SaldoDeEstoque(Long id, Long empresaId, Long produtoId, Quantidade quantidade, CustoUnitario custoMedio) {
        this.id = id;
        this.empresaId = Objects.requireNonNull(empresaId, "empresaId");
        this.produtoId = Objects.requireNonNull(produtoId, "produtoId");
        this.quantidade = Objects.requireNonNull(quantidade, "quantidade");
        this.custoMedio = Objects.requireNonNull(custoMedio, "custoMedio");
    }

    /** Produto que ainda não teve movimentação: saldo zero. */
    public static SaldoDeEstoque zerado(Long empresaId, Long produtoId) {
        return new SaldoDeEstoque(null, empresaId, produtoId, Quantidade.ZERO, CustoUnitario.ZERO);
    }

    public static SaldoDeEstoque reconstituir(Long id, Long empresaId, Long produtoId, Quantidade quantidade,
            CustoUnitario custoMedio) {
        return new SaldoDeEstoque(id, empresaId, produtoId, quantidade, custoMedio);
    }

    public Movimentacao movimentar(PedidoDeMovimentacao pedido, RegrasDeEstoque regras, Instant agora) {
        exigirQuePodeMovimentar(pedido, regras);
        CustoUnitario custo = pedido.tipo().soma() && pedido.custoUnitario() != null ? pedido.custoUnitario() : custoMedio;

        if (pedido.tipo().soma()) {
            custoMedio = mediaAposEntrada(pedido.quantidade(), custo);
            quantidade = quantidade.somar(pedido.quantidade());
        } else {
            if (pedido.tipo() == TipoDeMovimentacao.ESTORNO_DE_ENTRADA) {
                custo = Objects.requireNonNullElse(pedido.custoUnitario(), custoMedio);
                custoMedio = mediaAposDesfazerEntrada(pedido.quantidade(), custo);
            }
            quantidade = quantidade.subtrair(pedido.quantidade());
        }
        return new Movimentacao(null, empresaId, produtoId, pedido.tipo(), pedido.quantidade(), custo, quantidade,
                custoMedio, pedido.origem(), pedido.documentoDeOrigem(), pedido.motivo(), pedido.estornada(),
                pedido.usuarioId(), agora);
    }

    private void exigirQuePodeMovimentar(PedidoDeMovimentacao pedido, RegrasDeEstoque regras) {
        if (!regras.controlaEstoque()) {
            throw new ProdutoSemControleDeEstoqueException();
        }
        if (!regras.aceitaFracao() && !pedido.quantidade().inteira()) {
            throw new QuantidadeFracionadaException();
        }
        boolean ficariaNegativo = !pedido.tipo().soma() && quantidade.compareTo(pedido.quantidade()) < 0;
        if (ficariaNegativo && !regras.permiteNegativo()) {
            throw new EstoqueInsuficienteException(quantidade, pedido.quantidade());
        }
    }

    /** Com saldo zero ou negativo não há "média" a ponderar: vale o custo da entrada. */
    private CustoUnitario mediaAposEntrada(Quantidade entrada, CustoUnitario custoDaEntrada) {
        if (quantidade.compareTo(Quantidade.ZERO) <= 0) {
            return custoDaEntrada;
        }
        BigDecimal valorAtual = quantidade.valor().multiply(custoMedio.valor());
        BigDecimal valorDaEntrada = entrada.valor().multiply(custoDaEntrada.valor());
        BigDecimal novaQuantidade = quantidade.valor().add(entrada.valor());
        return new CustoUnitario(valorAtual.add(valorDaEntrada)
                .divide(novaQuantidade, CustoUnitario.CASAS_DECIMAIS, ARREDONDAMENTO));
    }

    /** Estorno de entrada: tira da média o que aquela entrada tinha colocado. */
    private CustoUnitario mediaAposDesfazerEntrada(Quantidade saida, CustoUnitario custoDaEntrada) {
        BigDecimal restante = quantidade.valor().subtract(saida.valor());
        if (restante.signum() <= 0) {
            return custoMedio;
        }
        BigDecimal valorRestante = quantidade.valor().multiply(custoMedio.valor())
                .subtract(saida.valor().multiply(custoDaEntrada.valor()));
        if (valorRestante.signum() < 0) {
            return custoMedio;
        }
        return new CustoUnitario(valorRestante.divide(restante, CustoUnitario.CASAS_DECIMAIS, ARREDONDAMENTO));
    }

    public Long id() {
        return id;
    }

    public Long empresaId() {
        return empresaId;
    }

    public Long produtoId() {
        return produtoId;
    }

    public Quantidade quantidade() {
        return quantidade;
    }

    public CustoUnitario custoMedio() {
        return custoMedio;
    }
}
