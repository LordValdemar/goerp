package br.com.goerp.compras.dominio;

import static br.com.goerp.compartilhado.dominio.Textos.obrigatorio;

import java.time.Instant;
import java.util.List;
import java.util.Objects;

import br.com.goerp.compartilhado.dominio.Dinheiro;

/**
 * Chegada de mercadoria de um pedido, com a nota do fornecedor. Gera entradas no estoque
 * (feitas por quem registra). Lançado errado, é estornado (uma vez) e o pedido volta a esperar.
 */
public class Recebimento {

    private final Long id;
    private final Long empresaId;
    private final Long pedidoId;
    private final String notaFiscal;
    private final List<ItemRecebido> itens;
    private final Long recebidoPor;
    private final Instant recebidoEm;
    private boolean estornado;
    private Instant estornadoEm;
    private String motivoDoEstorno;

    private Recebimento(Long id, Long empresaId, Long pedidoId, String notaFiscal, List<ItemRecebido> itens,
            Long recebidoPor, Instant recebidoEm, boolean estornado, Instant estornadoEm, String motivoDoEstorno) {
        this.id = id;
        this.empresaId = Objects.requireNonNull(empresaId, "empresaId");
        this.pedidoId = Objects.requireNonNull(pedidoId, "pedidoId");
        this.notaFiscal = obrigatorio(notaFiscal, "notaFiscal");
        if (itens == null || itens.isEmpty()) {
            throw new RecebimentoSemItensException();
        }
        this.itens = List.copyOf(itens);
        this.recebidoPor = Objects.requireNonNull(recebidoPor, "recebidoPor");
        this.recebidoEm = Objects.requireNonNull(recebidoEm, "recebidoEm");
        this.estornado = estornado;
        this.estornadoEm = estornadoEm;
        this.motivoDoEstorno = motivoDoEstorno;
    }

    public static Recebimento registrar(Long empresaId, Long pedidoId, String notaFiscal, List<ItemRecebido> itens,
            Long usuarioId, Instant agora) {
        return new Recebimento(null, empresaId, pedidoId, notaFiscal, itens, usuarioId, agora, false, null, null);
    }

    public static Recebimento reconstituir(Long id, Long empresaId, Long pedidoId, String notaFiscal,
            List<ItemRecebido> itens, Long recebidoPor, Instant recebidoEm, boolean estornado, Instant estornadoEm,
            String motivoDoEstorno) {
        return new Recebimento(id, empresaId, pedidoId, notaFiscal, itens, recebidoPor, recebidoEm, estornado,
                estornadoEm, motivoDoEstorno);
    }

    public void estornar(String motivo, Instant agora) {
        if (estornado) {
            throw new RecebimentoJaEstornadoException();
        }
        motivoDoEstorno = obrigatorio(motivo, "motivo");
        estornado = true;
        estornadoEm = agora;
    }

    public Dinheiro valorTotal() {
        return itens.stream().map(ItemRecebido::valor).reduce(Dinheiro.ZERO, Dinheiro::somar);
    }

    public Long id() {
        return id;
    }

    public Long empresaId() {
        return empresaId;
    }

    public Long pedidoId() {
        return pedidoId;
    }

    public String notaFiscal() {
        return notaFiscal;
    }

    public List<ItemRecebido> itens() {
        return itens;
    }

    public Long recebidoPor() {
        return recebidoPor;
    }

    public Instant recebidoEm() {
        return recebidoEm;
    }

    public boolean estornado() {
        return estornado;
    }

    public Instant estornadoEm() {
        return estornadoEm;
    }

    public String motivoDoEstorno() {
        return motivoDoEstorno;
    }
}
