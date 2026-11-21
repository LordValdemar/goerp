package br.com.goerp.compras.dominio;

import static br.com.goerp.compartilhado.dominio.Textos.obrigatorio;
import static br.com.goerp.compartilhado.dominio.Textos.opcional;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.function.BiFunction;

import br.com.goerp.compartilhado.dominio.Dinheiro;
import br.com.goerp.compartilhado.dominio.Quantidade;

/**
 * Pedido de compra a um fornecedor. Em rascunho, pode ser alterado; enviado, fica congelado e
 * acompanha o que já chegou de cada item. Cada produto aparece uma vez só no pedido.
 */
public class PedidoDeCompra {

    private final Long id;
    private final Long numero;
    private final Long empresaId;
    private final Long fornecedorId;
    private final Long compradorId;
    private final Instant criadoEm;
    private final Long versao;
    private List<ItemDeCompra> itens;
    private LocalDate previsaoDeEntrega;
    private String observacoes;
    private SituacaoDoPedidoDeCompra situacao;
    private Instant enviadoEm;
    private Instant encerradoEm;
    private String motivoDoEncerramento;

    /** Conteúdo alterável enquanto rascunho. */
    public record Conteudo(List<ItemDeCompra> itens, LocalDate previsaoDeEntrega, String observacoes) {
    }

    /** Situação e datas, para reconstituir do banco. */
    public record Andamento(SituacaoDoPedidoDeCompra situacao, Instant enviadoEm, Instant encerradoEm,
            String motivoDoEncerramento) {
    }

    private PedidoDeCompra(Long id, Long numero, Long empresaId, Long fornecedorId, Long compradorId, Instant criadoEm,
            Conteudo conteudo, Andamento andamento, Long versao) {
        this.id = id;
        this.numero = numero;
        this.empresaId = Objects.requireNonNull(empresaId, "empresaId");
        this.fornecedorId = Objects.requireNonNull(fornecedorId, "fornecedorId");
        this.compradorId = Objects.requireNonNull(compradorId, "compradorId");
        this.criadoEm = Objects.requireNonNull(criadoEm, "criadoEm");
        this.versao = versao;
        definirConteudo(conteudo);
        this.situacao = Objects.requireNonNull(andamento.situacao(), "situacao");
        this.enviadoEm = andamento.enviadoEm();
        this.encerradoEm = andamento.encerradoEm();
        this.motivoDoEncerramento = andamento.motivoDoEncerramento();
    }

    public static PedidoDeCompra rascunho(Long empresaId, Long numero, Long fornecedorId, Long compradorId,
            Conteudo conteudo, Instant agora) {
        return new PedidoDeCompra(null, numero, empresaId, fornecedorId, compradorId, agora, conteudo,
                new Andamento(SituacaoDoPedidoDeCompra.RASCUNHO, null, null, null), null);
    }

    public static PedidoDeCompra reconstituir(Long id, Long numero, Long empresaId, Long fornecedorId, Long compradorId,
            Instant criadoEm, Conteudo conteudo, Andamento andamento, Long versao) {
        return new PedidoDeCompra(id, numero, empresaId, fornecedorId, compradorId, criadoEm, conteudo, andamento,
                versao);
    }

    public void alterar(Conteudo conteudo) {
        exigir("alterado", SituacaoDoPedidoDeCompra.RASCUNHO);
        definirConteudo(conteudo);
    }

    public void enviar(Instant agora) {
        exigir("enviado", SituacaoDoPedidoDeCompra.RASCUNHO);
        situacao = SituacaoDoPedidoDeCompra.ENVIADO;
        enviadoEm = agora;
    }

    /** Cancela o pedido inteiro; só se nada chegou ainda. */
    public void cancelar(String motivo, Instant agora) {
        exigir("cancelado", SituacaoDoPedidoDeCompra.RASCUNHO, SituacaoDoPedidoDeCompra.ENVIADO);
        encerrar(SituacaoDoPedidoDeCompra.CANCELADO, motivo, agora);
    }

    /** O fornecedor não vai entregar o restante: o que falta deixa de ser esperado. */
    public void encerrarSaldo(String motivo, Instant agora) {
        exigir("encerrado", SituacaoDoPedidoDeCompra.PARCIALMENTE_RECEBIDO);
        encerrar(SituacaoDoPedidoDeCompra.ENCERRADO, motivo, agora);
    }

    /** Registra a chegada de mercadoria; quem chama dá a entrada no estoque. */
    public void receber(List<ItemRecebido> recebidos) {
        exigir("recebido", SituacaoDoPedidoDeCompra.ENVIADO, SituacaoDoPedidoDeCompra.PARCIALMENTE_RECEBIDO);
        aplicar(recebidos, ItemDeCompra::receber);
        situacao = itens.stream().allMatch(ItemDeCompra::completo) ? SituacaoDoPedidoDeCompra.RECEBIDO
                : SituacaoDoPedidoDeCompra.PARCIALMENTE_RECEBIDO;
    }

    /** Desfaz um recebimento lançado errado: o que ele trouxe volta a ser esperado. */
    public void estornarRecebimento(List<ItemRecebido> recebidos) {
        exigir("ter recebimento estornado", SituacaoDoPedidoDeCompra.PARCIALMENTE_RECEBIDO,
                SituacaoDoPedidoDeCompra.RECEBIDO);
        aplicar(recebidos, ItemDeCompra::desfazerRecebimento);
        boolean nadaRecebido = itens.stream().allMatch(item -> item.recebida().compareTo(Quantidade.ZERO) == 0);
        situacao = nadaRecebido ? SituacaoDoPedidoDeCompra.ENVIADO : SituacaoDoPedidoDeCompra.PARCIALMENTE_RECEBIDO;
    }

    public Dinheiro total() {
        return itens.stream().map(ItemDeCompra::valor).reduce(Dinheiro.ZERO, Dinheiro::somar);
    }

    /** Quanto ainda falta receber, em valor (zero se o pedido não espera mais entrega). */
    public Dinheiro valorPendente() {
        if (!situacao.aguardandoEntrega()) {
            return Dinheiro.ZERO;
        }
        return itens.stream().map(ItemDeCompra::valorPendente).reduce(Dinheiro.ZERO, Dinheiro::somar);
    }

    public ItemDeCompra item(Long produtoId) {
        return itens.stream().filter(item -> item.produtoId().equals(produtoId)).findFirst()
                .orElseThrow(() -> new ProdutoForaDoPedidoException(produtoId));
    }

    /** Calcula tudo numa cópia e só aplica no fim: se um item for recusado, o pedido não muda. */
    private void aplicar(List<ItemRecebido> recebidos, BiFunction<ItemDeCompra, Quantidade, ItemDeCompra> operacao) {
        if (recebidos.isEmpty()) {
            throw new RecebimentoSemItensException();
        }
        List<ItemDeCompra> atualizados = new ArrayList<>(itens);
        for (ItemRecebido recebido : recebidos) {
            int posicao = posicaoDo(recebido.produtoId(), atualizados);
            atualizados.set(posicao, operacao.apply(atualizados.get(posicao), recebido.quantidade()));
        }
        itens = List.copyOf(atualizados);
    }

    private static int posicaoDo(Long produtoId, List<ItemDeCompra> lista) {
        for (int i = 0; i < lista.size(); i++) {
            if (lista.get(i).produtoId().equals(produtoId)) {
                return i;
            }
        }
        throw new ProdutoForaDoPedidoException(produtoId);
    }

    private void encerrar(SituacaoDoPedidoDeCompra nova, String motivo, Instant agora) {
        motivoDoEncerramento = obrigatorio(motivo, "motivo");
        situacao = nova;
        encerradoEm = agora;
    }

    private void definirConteudo(Conteudo conteudo) {
        if (conteudo.itens() == null || conteudo.itens().isEmpty()) {
            throw new PedidoDeCompraSemItensException();
        }
        var produtos = new HashSet<Long>();
        conteudo.itens().forEach(item -> {
            if (!produtos.add(item.produtoId())) {
                throw new ProdutoRepetidoNoPedidoException();
            }
        });
        this.itens = List.copyOf(conteudo.itens());
        this.previsaoDeEntrega = conteudo.previsaoDeEntrega();
        this.observacoes = opcional(conteudo.observacoes());
    }

    private void exigir(String acao, SituacaoDoPedidoDeCompra... permitidas) {
        if (!List.of(permitidas).contains(situacao)) {
            throw new TransicaoDeCompraInvalidaException(situacao, acao);
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

    public Long fornecedorId() {
        return fornecedorId;
    }

    public Long compradorId() {
        return compradorId;
    }

    public Instant criadoEm() {
        return criadoEm;
    }

    public Long versao() {
        return versao;
    }

    public List<ItemDeCompra> itens() {
        return itens;
    }

    public Conteudo conteudo() {
        return new Conteudo(itens, previsaoDeEntrega, observacoes);
    }

    public Andamento andamento() {
        return new Andamento(situacao, enviadoEm, encerradoEm, motivoDoEncerramento);
    }

    public SituacaoDoPedidoDeCompra situacao() {
        return situacao;
    }

    /** Enviado (ou parcialmente recebido) com a previsão de entrega já passada. */
    public boolean atrasado(LocalDate hoje) {
        return situacao.aguardandoEntrega() && previsaoDeEntrega != null && hoje.isAfter(previsaoDeEntrega);
    }
}
