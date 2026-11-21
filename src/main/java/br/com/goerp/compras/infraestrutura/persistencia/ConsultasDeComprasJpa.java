package br.com.goerp.compras.infraestrutura.persistencia;

import java.time.Instant;
import java.util.List;

import org.springframework.stereotype.Repository;

import br.com.goerp.compras.aplicacao.ConsultasDeCompras;
import br.com.goerp.compras.aplicacao.RecebimentosDoPeriodo.Linha;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;

/** Recebimentos com o fornecedor e o número do pedido, numa consulta só (as duas entidades têm @TenantId). */
@Repository
@RequiredArgsConstructor
class ConsultasDeComprasJpa implements ConsultasDeCompras {

    private final EntityManager entityManager;
    private final RecebimentoRepositorioJpa recebimentos;

    @Override
    public List<Linha> recebimentos(Instant de, Instant ate, Long fornecedorId, boolean incluirEstornados) {
        return entityManager.createQuery("""
                SELECT r.id, p.fornecedorId, p.numero
                FROM Recebimento r JOIN PedidoDeCompra p ON p.id = r.pedidoId
                WHERE r.recebidoEm >= :de AND r.recebidoEm < :ate
                  AND (:todosOsFornecedores = true OR p.fornecedorId = :fornecedorId)
                  AND (:incluirEstornados = true OR r.estornado = false)
                ORDER BY r.recebidoEm DESC, r.id DESC""", Object[].class)
                .setParameter("de", de)
                .setParameter("ate", ate)
                .setParameter("todosOsFornecedores", fornecedorId == null)
                .setParameter("fornecedorId", fornecedorId == null ? -1L : fornecedorId)
                .setParameter("incluirEstornados", incluirEstornados)
                .getResultList().stream()
                .map(linha -> new Linha(recebimentos.buscarPorId((Long) linha[0]).orElseThrow(), (Long) linha[1],
                        (Long) linha[2]))
                .toList();
    }
}
