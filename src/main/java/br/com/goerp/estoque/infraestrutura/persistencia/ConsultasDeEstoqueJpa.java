package br.com.goerp.estoque.infraestrutura.persistencia;

import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;

import org.springframework.stereotype.Repository;

import br.com.goerp.cadastros.produto.dominio.CustoUnitario;
import br.com.goerp.compartilhado.dominio.Pagina;
import br.com.goerp.compartilhado.dominio.Paginacao;
import br.com.goerp.compartilhado.dominio.Quantidade;
import br.com.goerp.estoque.aplicacao.ConsultasDeEstoque;
import br.com.goerp.estoque.aplicacao.DivergenciaDeSaldo;
import br.com.goerp.estoque.aplicacao.FiltroDePosicoes;
import br.com.goerp.estoque.aplicacao.PosicaoDeEstoque;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import lombok.RequiredArgsConstructor;

/**
 * Consultas de leitura em JPQL. Todas as entidades envolvidas têm {@code @TenantId}, então o
 * Hibernate filtra cada uma pela empresa atual: não há filtro manual de empresa aqui.
 * Parâmetros nunca são nulos (texto vazio e booleano), o que evita o problema de tipo do PostgreSQL.
 */
@Repository
@RequiredArgsConstructor
class ConsultasDeEstoqueJpa implements ConsultasDeEstoque {

    private static final String DE_POSICOES = """
            FROM Produto p
            JOIN UnidadeDeMedida u ON u.id = p.unidadeId
            LEFT JOIN SaldoDeEstoque s ON s.produtoId = p.id
            WHERE p.controlaEstoque = true AND p.ativo = true
              AND (:texto = '' OR LOWER(p.descricao) LIKE :padrao OR LOWER(p.codigo) LIKE :padrao)
              AND (:somenteARepor = false OR COALESCE(s.quantidade, 0) <= CAST(p.estoqueMinimo AS BigDecimal))
            """;

    private static final String SOMA_DAS_MOVIMENTACOES = """
            SELECT s.produtoId, s.quantidade,
                   COALESCE(SUM(CASE WHEN m.tipo IN (
                           br.com.goerp.estoque.dominio.TipoDeMovimentacao.ENTRADA,
                           br.com.goerp.estoque.dominio.TipoDeMovimentacao.AJUSTE_DE_ENTRADA,
                           br.com.goerp.estoque.dominio.TipoDeMovimentacao.ESTORNO_DE_SAIDA)
                       THEN m.quantidade ELSE -m.quantidade END), 0)
            FROM SaldoDeEstoque s
            LEFT JOIN MovimentacaoDeEstoque m ON m.produtoId = s.produtoId
            GROUP BY s.produtoId, s.quantidade
            """;

    private final EntityManager entityManager;

    @Override
    public Pagina<PosicaoDeEstoque> posicoes(FiltroDePosicoes filtro, Paginacao paginacao) {
        TypedQuery<Object[]> itens = entityManager.createQuery("""
                SELECT p.id, p.codigo, p.descricao, u.sigla, COALESCE(s.quantidade, 0), COALESCE(s.custoMedio, 0),
                       p.estoqueMinimo
                """ + DE_POSICOES + " ORDER BY p.descricao, p.id", Object[].class);
        TypedQuery<Long> total = entityManager.createQuery("SELECT COUNT(p) " + DE_POSICOES, Long.class);
        aplicarFiltro(itens, filtro);
        aplicarFiltro(total, filtro);

        List<PosicaoDeEstoque> pagina = itens
                .setFirstResult(paginacao.numero() * paginacao.tamanho())
                .setMaxResults(paginacao.tamanho())
                .getResultList().stream()
                .map(ConsultasDeEstoqueJpa::paraPosicao)
                .toList();
        return new Pagina<>(pagina, paginacao.numero(), paginacao.tamanho(), total.getSingleResult());
    }

    @Override
    public List<DivergenciaDeSaldo> divergencias() {
        return entityManager.createQuery(SOMA_DAS_MOVIMENTACOES, Object[].class).getResultList().stream()
                .map(linha -> new DivergenciaDeSaldo((Long) linha[0], (BigDecimal) linha[1], (BigDecimal) linha[2]))
                .filter(linha -> linha.saldoGravado().compareTo(linha.somaDasMovimentacoes()) != 0)
                .toList();
    }

    private static void aplicarFiltro(TypedQuery<?> consulta, FiltroDePosicoes filtro) {
        consulta.setParameter("texto", filtro.texto());
        consulta.setParameter("padrao", "%" + filtro.texto().toLowerCase(Locale.ROOT) + "%");
        consulta.setParameter("somenteARepor", filtro.somenteARepor());
    }

    private static PosicaoDeEstoque paraPosicao(Object[] linha) {
        return new PosicaoDeEstoque((Long) linha[0], (String) linha[1], (String) linha[2], (String) linha[3],
                new Quantidade(numero(linha[4])), new CustoUnitario(numero(linha[5])), (Quantidade) linha[6]);
    }

    private static BigDecimal numero(Object valor) {
        return valor instanceof BigDecimal decimal ? decimal : new BigDecimal(valor.toString());
    }
}
