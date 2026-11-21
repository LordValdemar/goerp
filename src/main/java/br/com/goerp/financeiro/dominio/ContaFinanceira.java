package br.com.goerp.financeiro.dominio;

import static br.com.goerp.compartilhado.dominio.Textos.obrigatorio;

import java.time.Instant;
import java.util.Objects;

import br.com.goerp.compartilhado.dominio.Dinheiro;

/**
 * Onde o dinheiro da empresa está (caixa, banco, conta de pagamento). O saldo só muda
 * registrando um {@link Lancamento}: é a única porta de entrada, e cada lançamento guarda o saldo resultante.
 */
public class ContaFinanceira {

    private final Long id;
    private final Long empresaId;
    private final TipoDeConta tipo;
    private String nome;
    private boolean permiteSaldoNegativo;
    private Dinheiro saldo;
    private boolean ativa;

    private ContaFinanceira(Long id, Long empresaId, TipoDeConta tipo, String nome, boolean permiteSaldoNegativo,
            Dinheiro saldo, boolean ativa) {
        this.id = id;
        this.empresaId = Objects.requireNonNull(empresaId, "empresaId");
        this.tipo = Objects.requireNonNull(tipo, "tipo");
        this.saldo = Objects.requireNonNull(saldo, "saldo");
        this.ativa = ativa;
        alterar(nome, permiteSaldoNegativo);
    }

    /** Toda conta nasce zerada: um saldo inicial entra como lançamento, para ficar no extrato. */
    public static ContaFinanceira nova(Long empresaId, TipoDeConta tipo, String nome, boolean permiteSaldoNegativo) {
        return new ContaFinanceira(null, empresaId, tipo, nome, permiteSaldoNegativo, Dinheiro.ZERO, true);
    }

    public static ContaFinanceira reconstituir(Long id, Long empresaId, TipoDeConta tipo, String nome,
            boolean permiteSaldoNegativo, Dinheiro saldo, boolean ativa) {
        return new ContaFinanceira(id, empresaId, tipo, nome, permiteSaldoNegativo, saldo, ativa);
    }

    /** Saldo negativo só faz sentido em banco com cheque especial; nas demais contas é erro de digitação. */
    public void alterar(String novoNome, boolean aceitaSaldoNegativo) {
        this.nome = obrigatorio(novoNome, "nome");
        this.permiteSaldoNegativo = aceitaSaldoNegativo;
    }

    public void inativar() {
        if (saldo.compareTo(Dinheiro.ZERO) != 0) {
            throw new ContaComSaldoException(saldo);
        }
        ativa = false;
    }

    public void reativar() {
        ativa = true;
    }

    public Lancamento lancar(PedidoDeLancamento pedido, Instant agora) {
        exigirQuePodeLancar(pedido);
        saldo = saldo.somar(pedido.tipo().soma() ? pedido.valor() : Dinheiro.ZERO.subtrair(pedido.valor()));
        return new Lancamento(null, empresaId, id, pedido.tipo(), pedido.valor(), saldo, pedido.forma(),
                pedido.categoriaId(), pedido.descricao(), pedido.origem(), pedido.documentoDeOrigem(),
                pedido.estornado(), pedido.usuarioId(), agora);
    }

    private void exigirQuePodeLancar(PedidoDeLancamento pedido) {
        if (!ativa) {
            throw new ContaFinanceiraInativaException(nome);
        }
        boolean ficariaNegativo = !pedido.tipo().soma() && saldo.compareTo(pedido.valor()) < 0;
        if (ficariaNegativo && !permiteSaldoNegativo) {
            throw new SaldoInsuficienteException(saldo, pedido.valor());
        }
    }

    public Long id() {
        return id;
    }

    public Long empresaId() {
        return empresaId;
    }

    public TipoDeConta tipo() {
        return tipo;
    }

    public String nome() {
        return nome;
    }

    public boolean permiteSaldoNegativo() {
        return permiteSaldoNegativo;
    }

    public Dinheiro saldo() {
        return saldo;
    }

    public boolean ativa() {
        return ativa;
    }
}
