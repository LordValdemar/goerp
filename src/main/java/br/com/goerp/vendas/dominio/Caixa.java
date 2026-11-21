package br.com.goerp.vendas.dominio;

import static br.com.goerp.compartilhado.dominio.Textos.obrigatorio;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import br.com.goerp.compartilhado.dominio.Dinheiro;
import br.com.goerp.financeiro.dominio.FormaDePagamento;

/**
 * Sessão de caixa de um operador: começa com o troco inicial, recebe as vendas, suprimentos e
 * sangrias, e termina no fechamento, em que o operador informa quanto contou em cada forma de
 * pagamento e o sistema registra a diferença em relação ao esperado.
 */
public class Caixa {

    private final Long id;
    private final Long empresaId;
    private final Long operadorId;
    private final Dinheiro trocoInicial;
    private final Instant abertoEm;
    private final List<MovimentoDeCaixa> movimentos;
    private SituacaoDoCaixa situacao;
    private Instant fechadoEm;
    private Long fechadoPor;
    private List<ConferenciaDeCaixa> conferencias;

    private Caixa(Long id, Long empresaId, Long operadorId, Dinheiro trocoInicial, Instant abertoEm,
            List<MovimentoDeCaixa> movimentos, SituacaoDoCaixa situacao, Instant fechadoEm, Long fechadoPor,
            List<ConferenciaDeCaixa> conferencias) {
        this.id = id;
        this.empresaId = Objects.requireNonNull(empresaId, "empresaId");
        this.operadorId = Objects.requireNonNull(operadorId, "operadorId");
        this.trocoInicial = Objects.requireNonNull(trocoInicial, "trocoInicial").exigirNaoNegativo("trocoInicial");
        this.abertoEm = Objects.requireNonNull(abertoEm, "abertoEm");
        this.movimentos = new ArrayList<>(movimentos);
        this.situacao = Objects.requireNonNull(situacao, "situacao");
        this.fechadoEm = fechadoEm;
        this.fechadoPor = fechadoPor;
        this.conferencias = List.copyOf(conferencias);
    }

    public static Caixa abrir(Long empresaId, Long operadorId, Dinheiro trocoInicial, Instant agora) {
        return new Caixa(null, empresaId, operadorId, trocoInicial, agora, List.of(), SituacaoDoCaixa.ABERTO, null,
                null, List.of());
    }

    public static Caixa reconstituir(Long id, Long empresaId, Long operadorId, Dinheiro trocoInicial, Instant abertoEm,
            List<MovimentoDeCaixa> movimentos, SituacaoDoCaixa situacao, Instant fechadoEm, Long fechadoPor,
            List<ConferenciaDeCaixa> conferencias) {
        return new Caixa(id, empresaId, operadorId, trocoInicial, abertoEm, movimentos, situacao, fechadoEm,
                fechadoPor, conferencias);
    }

    public void registrarSuprimento(Dinheiro valor, String motivo, Long usuarioId, Instant agora) {
        exigirAberto();
        movimentos.add(new MovimentoDeCaixa(TipoDeMovimentoDeCaixa.SUPRIMENTO, valor, motivo, usuarioId, agora));
    }

    /**
     * @param recebidoPelasVendas o que as vendas deste caixa trouxeram, por forma (para saber quanto dinheiro há)
     */
    public void registrarSangria(Dinheiro valor, String motivo, Long usuarioId, Instant agora,
            Map<FormaDePagamento, Dinheiro> recebidoPelasVendas) {
        exigirAberto();
        var sangria = new MovimentoDeCaixa(TipoDeMovimentoDeCaixa.SANGRIA, valor, obrigatorio(motivo, "motivo"),
                usuarioId, agora);
        if (valor.compareTo(esperadoEm(FormaDePagamento.DINHEIRO, recebidoPelasVendas)) > 0) {
            throw new SangriaMaiorQueODinheiroException();
        }
        movimentos.add(sangria);
    }

    /**
     * Fecha o caixa comparando, por forma de pagamento, o que o operador contou com o esperado.
     * Forma não informada conta como zero (ex.: nenhum comprovante de cartão).
     */
    public List<ConferenciaDeCaixa> fechar(Map<FormaDePagamento, Dinheiro> contado,
            Map<FormaDePagamento, Dinheiro> recebidoPelasVendas, Long usuarioId, Instant agora) {
        exigirAberto();
        contado.keySet().stream().filter(forma -> !forma.conferidaNoCaixa()).findFirst().ifPresent(forma -> {
            throw new FormaNaoAceitaNoPdvException(forma);
        });
        conferencias = FormaDePagamento.conferidasNoCaixa().stream()
                .map(forma -> new ConferenciaDeCaixa(forma, esperadoEm(forma, recebidoPelasVendas),
                        contado.getOrDefault(forma, Dinheiro.ZERO)))
                .toList();
        situacao = SituacaoDoCaixa.FECHADO;
        fechadoEm = agora;
        fechadoPor = Objects.requireNonNull(usuarioId, "usuarioId");
        return conferencias;
    }

    /** Dinheiro esperado = troco inicial + suprimentos - sangrias + vendas em dinheiro (já sem troco). */
    public Dinheiro esperadoEm(FormaDePagamento forma, Map<FormaDePagamento, Dinheiro> recebidoPelasVendas) {
        Dinheiro vendas = recebidoPelasVendas.getOrDefault(forma, Dinheiro.ZERO);
        if (forma != FormaDePagamento.DINHEIRO) {
            return vendas;
        }
        return trocoInicial.somar(totalDe(TipoDeMovimentoDeCaixa.SUPRIMENTO))
                .subtrair(totalDe(TipoDeMovimentoDeCaixa.SANGRIA))
                .somar(vendas);
    }

    public boolean aberto() {
        return situacao == SituacaoDoCaixa.ABERTO;
    }

    public void exigirAberto() {
        if (!aberto()) {
            throw new CaixaFechadoException();
        }
    }

    private Dinheiro totalDe(TipoDeMovimentoDeCaixa tipo) {
        return movimentos.stream().filter(movimento -> movimento.tipo() == tipo).map(MovimentoDeCaixa::valor)
                .reduce(Dinheiro.ZERO, Dinheiro::somar);
    }

    public Long id() {
        return id;
    }

    public Long empresaId() {
        return empresaId;
    }

    public Long operadorId() {
        return operadorId;
    }

    public Dinheiro trocoInicial() {
        return trocoInicial;
    }

    public Instant abertoEm() {
        return abertoEm;
    }

    public List<MovimentoDeCaixa> movimentos() {
        return List.copyOf(movimentos);
    }

    public SituacaoDoCaixa situacao() {
        return situacao;
    }

    public Instant fechadoEm() {
        return fechadoEm;
    }

    public Long fechadoPor() {
        return fechadoPor;
    }

    public List<ConferenciaDeCaixa> conferencias() {
        return conferencias;
    }
}
