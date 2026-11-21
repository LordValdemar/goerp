package br.com.goerp.financeiro.dominio;

import static br.com.goerp.compartilhado.dominio.Textos.obrigatorio;
import static br.com.goerp.compartilhado.dominio.Textos.opcional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import br.com.goerp.compartilhado.dominio.Dinheiro;
import br.com.goerp.compartilhado.dominio.excecao.ValorInvalidoException;

/**
 * Uma parcela de conta a pagar ou a receber. Cada pagamento é uma {@link Baixa}; a situação
 * (aberto, parcialmente pago, quitado) decorre do que já foi abatido do valor.
 */
public class Titulo {

    private final Long id;
    private final Long empresaId;
    private final NaturezaDoTitulo natureza;
    private final Long pessoaId;
    private final FormaDePagamento formaPrevista;
    private final OrigemDoTitulo origem;
    private final Long documentoDeOrigem;
    private final int parcela;
    private final int parcelas;
    private String descricao;
    private Long categoriaId;
    private String documento;
    private Dinheiro valor;
    private LocalDate vencimento;
    private SituacaoDoTitulo situacao;
    private final List<Baixa> baixas;
    private String motivoDoCancelamento;
    private Instant canceladoEm;

    private Titulo(Long id, Long empresaId, DadosDoTitulo dados, Parcela parcela, SituacaoDoTitulo situacao,
            List<Baixa> baixas, String motivoDoCancelamento, Instant canceladoEm) {
        this.id = id;
        this.empresaId = Objects.requireNonNull(empresaId, "empresaId");
        this.natureza = dados.natureza();
        this.pessoaId = dados.pessoaId();
        this.formaPrevista = dados.formaPrevista();
        this.origem = dados.origem();
        this.documentoDeOrigem = dados.documentoDeOrigem();
        this.parcela = parcela.numero();
        this.parcelas = parcela.de();
        this.descricao = dados.descricao();
        this.categoriaId = dados.categoriaId();
        this.documento = dados.documento();
        this.valor = parcela.valor();
        this.vencimento = parcela.vencimento();
        this.situacao = Objects.requireNonNull(situacao, "situacao");
        this.baixas = new ArrayList<>(baixas);
        this.motivoDoCancelamento = motivoDoCancelamento;
        this.canceladoEm = canceladoEm;
    }

    /** Um título por parcela, todos com os mesmos dados. */
    public static List<Titulo> parcelas(Long empresaId, DadosDoTitulo dados, Parcelamento parcelamento) {
        return parcelamento.parcelas().stream()
                .map(parcela -> new Titulo(null, empresaId, dados, parcela, SituacaoDoTitulo.ABERTO, List.of(), null, null))
                .toList();
    }

    public static Titulo reconstituir(Long id, Long empresaId, DadosDoTitulo dados, Parcela parcela,
            SituacaoDoTitulo situacao, List<Baixa> baixas, String motivoDoCancelamento, Instant canceladoEm) {
        return new Titulo(id, empresaId, dados, parcela, situacao, baixas, motivoDoCancelamento, canceladoEm);
    }

    /** Corrige o título antes de qualquer pagamento (depois, o valor já foi parcialmente abatido). */
    public void alterar(String novaDescricao, Long novaCategoria, String novoDocumento, Dinheiro novoValor,
            LocalDate novoVencimento) {
        exigirSemPagamentos();
        Objects.requireNonNull(novoValor, "valor");
        if (novoValor.compareTo(Dinheiro.ZERO) <= 0) {
            throw new ValorInvalidoException("O valor do título precisa ser maior que zero");
        }
        descricao = obrigatorio(novaDescricao, "descricao");
        categoriaId = novaCategoria;
        documento = opcional(novoDocumento);
        valor = novoValor;
        vencimento = Objects.requireNonNull(novoVencimento, "vencimento");
    }

    public Baixa baixar(PedidoDeBaixa pedido, Instant agora) {
        exigirEmAberto();
        Dinheiro devido = saldoDevedor();
        if (pedido.valorAmortizado().compareTo(devido) > 0) {
            throw new BaixaAcimaDoSaldoException(devido, pedido.valorAmortizado());
        }
        Baixa baixa = Baixa.de(baixas.size() + 1, pedido, agora);
        baixas.add(baixa);
        recalcularSituacao();
        return baixa;
    }

    /** Desfaz um pagamento (ex.: cheque devolvido, lançado no título errado). */
    public Baixa estornarBaixa(int numero, String motivo, Instant agora) {
        if (situacao == SituacaoDoTitulo.CANCELADO) {
            throw new TituloNaoAbertoException(situacao);
        }
        String justificativa = obrigatorio(motivo, "motivo");
        Baixa baixa = baixa(numero);
        Baixa estornada = baixa.estornar(justificativa, agora);
        baixas.set(baixas.indexOf(baixa), estornada);
        recalcularSituacao();
        return estornada;
    }

    public void cancelar(String motivo, Instant agora) {
        exigirSemPagamentos();
        motivoDoCancelamento = obrigatorio(motivo, "motivo");
        canceladoEm = Objects.requireNonNull(agora, "agora");
        situacao = SituacaoDoTitulo.CANCELADO;
    }

    public Dinheiro saldoDevedor() {
        if (situacao == SituacaoDoTitulo.CANCELADO) {
            return Dinheiro.ZERO;
        }
        return valor.subtrair(baixasValidas().stream().map(Baixa::valorAmortizado).reduce(Dinheiro.ZERO, Dinheiro::somar));
    }

    /** O que efetivamente entrou ou saiu do caixa por este título (com juros e multa, sem desconto). */
    public Dinheiro totalPago() {
        return baixasValidas().stream().map(Baixa::valorPago).reduce(Dinheiro.ZERO, Dinheiro::somar);
    }

    public boolean vencido(LocalDate hoje) {
        return situacao.emAberto() && vencimento.isBefore(hoje);
    }

    public long diasEmAtraso(LocalDate hoje) {
        return vencido(hoje) ? ChronoUnit.DAYS.between(vencimento, hoje) : 0;
    }

    public Baixa baixa(int numero) {
        return baixas.stream().filter(b -> b.numero() == numero).findFirst()
                .orElseThrow(() -> new ValorInvalidoException("O título não tem o pagamento " + numero));
    }

    private List<Baixa> baixasValidas() {
        return baixas.stream().filter(b -> !b.estornada()).toList();
    }

    private void recalcularSituacao() {
        if (baixasValidas().isEmpty()) {
            situacao = SituacaoDoTitulo.ABERTO;
        } else {
            situacao = saldoDevedor().compareTo(Dinheiro.ZERO) == 0 ? SituacaoDoTitulo.QUITADO
                    : SituacaoDoTitulo.PARCIALMENTE_PAGO;
        }
    }

    private void exigirEmAberto() {
        if (!situacao.emAberto()) {
            throw new TituloNaoAbertoException(situacao);
        }
    }

    private void exigirSemPagamentos() {
        exigirEmAberto();
        if (!baixasValidas().isEmpty()) {
            throw new TituloComBaixaException();
        }
    }

    public DadosDoTitulo dados() {
        return new DadosDoTitulo(natureza, pessoaId, descricao, categoriaId, documento, formaPrevista, origem,
                documentoDeOrigem);
    }

    public Parcela parcelaAtual() {
        return new Parcela(parcela, parcelas, valor, vencimento);
    }

    public Long id() {
        return id;
    }

    public Long empresaId() {
        return empresaId;
    }

    public NaturezaDoTitulo natureza() {
        return natureza;
    }

    public Long pessoaId() {
        return pessoaId;
    }

    public FormaDePagamento formaPrevista() {
        return formaPrevista;
    }

    public OrigemDoTitulo origem() {
        return origem;
    }

    public Long documentoDeOrigem() {
        return documentoDeOrigem;
    }

    public int parcela() {
        return parcela;
    }

    public int parcelas() {
        return parcelas;
    }

    public String descricao() {
        return descricao;
    }

    public Long categoriaId() {
        return categoriaId;
    }

    public String documento() {
        return documento;
    }

    public Dinheiro valor() {
        return valor;
    }

    public LocalDate vencimento() {
        return vencimento;
    }

    public SituacaoDoTitulo situacao() {
        return situacao;
    }

    public List<Baixa> baixas() {
        return List.copyOf(baixas);
    }

    public String motivoDoCancelamento() {
        return motivoDoCancelamento;
    }

    public Instant canceladoEm() {
        return canceladoEm;
    }
}
