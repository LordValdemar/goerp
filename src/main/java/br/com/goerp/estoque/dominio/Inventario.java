package br.com.goerp.estoque.dominio;

import static br.com.goerp.compartilhado.dominio.Textos.obrigatorio;

import java.time.Instant;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

import br.com.goerp.compartilhado.dominio.Quantidade;
import br.com.goerp.compartilhado.dominio.excecao.ValorInvalidoException;

/**
 * Contagem física do estoque. Enquanto aberto, recebe as quantidades contadas (pode-se recontar);
 * ao finalizar, o estoque de cada produto CONTADO é ajustado para a quantidade contada.
 * Produtos não contados não mudam: serve tanto para inventário geral quanto parcial.
 */
public class Inventario {

    private final Long id;
    private final Long empresaId;
    private final String descricao;
    private final Instant abertoEm;
    private final Long versao;
    private final Map<Long, Quantidade> contagens;
    private SituacaoDoInventario situacao;
    private Instant encerradoEm;

    private Inventario(Long id, Long empresaId, String descricao, Instant abertoEm, Map<Long, Quantidade> contagens,
            SituacaoDoInventario situacao, Instant encerradoEm, Long versao) {
        this.id = id;
        this.empresaId = Objects.requireNonNull(empresaId, "empresaId");
        this.descricao = obrigatorio(descricao, "descricao");
        this.abertoEm = Objects.requireNonNull(abertoEm, "abertoEm");
        this.contagens = new LinkedHashMap<>(contagens);
        this.situacao = Objects.requireNonNull(situacao, "situacao");
        this.encerradoEm = encerradoEm;
        this.versao = versao;
    }

    public static Inventario abrir(Long empresaId, String descricao, Instant agora) {
        return new Inventario(null, empresaId, descricao, agora, Map.of(), SituacaoDoInventario.ABERTO, null, null);
    }

    public static Inventario reconstituir(Long id, Long empresaId, String descricao, Instant abertoEm,
            Map<Long, Quantidade> contagens, SituacaoDoInventario situacao, Instant encerradoEm, Long versao) {
        return new Inventario(id, empresaId, descricao, abertoEm, contagens, situacao, encerradoEm, versao);
    }

    /** Registra (ou corrige) a quantidade contada de um produto. */
    public void registrarContagem(Long produtoId, Quantidade contada) {
        exigirAberto();
        if (contada.negativa()) {
            throw new ValorInvalidoException("A quantidade contada não pode ser negativa");
        }
        contagens.put(Objects.requireNonNull(produtoId, "produtoId"), contada);
    }

    /** Encerra a contagem; quem finaliza ajusta o estoque de cada produto contado. */
    public Map<Long, Quantidade> finalizar(Instant agora) {
        exigirAberto();
        if (contagens.isEmpty()) {
            throw new InventarioSemContagemException();
        }
        situacao = SituacaoDoInventario.FINALIZADO;
        encerradoEm = agora;
        return contagens();
    }

    public void cancelar(Instant agora) {
        exigirAberto();
        situacao = SituacaoDoInventario.CANCELADO;
        encerradoEm = agora;
    }

    private void exigirAberto() {
        if (situacao != SituacaoDoInventario.ABERTO) {
            throw new InventarioEncerradoException();
        }
    }

    public Long id() {
        return id;
    }

    public Long empresaId() {
        return empresaId;
    }

    public String descricao() {
        return descricao;
    }

    public Instant abertoEm() {
        return abertoEm;
    }

    public Map<Long, Quantidade> contagens() {
        return Collections.unmodifiableMap(contagens);
    }

    public SituacaoDoInventario situacao() {
        return situacao;
    }

    public Instant encerradoEm() {
        return encerradoEm;
    }

    public Long versao() {
        return versao;
    }
}
