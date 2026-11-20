package br.com.goerp.estoque.infraestrutura.persistencia;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import jakarta.persistence.LockModeType;

interface SaldoDeEstoqueJpaRepository extends JpaRepository<SaldoDeEstoqueJpa, Long> {

    Optional<SaldoDeEstoqueJpa> findByProdutoId(Long produtoId);

    /** SELECT ... FOR UPDATE: segura a linha até o fim da transação. */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM SaldoDeEstoque s WHERE s.produtoId = :produtoId")
    Optional<SaldoDeEstoqueJpa> travar(Long produtoId);

    /**
     * Garante que a linha existe sem disputa: se duas transações tentarem criar ao mesmo tempo,
     * a segunda não faz nada (em vez de falhar por chave duplicada). SQL nativo não passa pelo filtro
     * de empresa do Hibernate, por isso a empresa é informada explicitamente.
     */
    @Modifying
    @Query(value = """
            INSERT INTO saldo_estoque (empresa_id, produto_id, quantidade, custo_medio, criado_em, atualizado_em)
            VALUES (:empresaId, :produtoId, 0, 0, now(), now())
            ON CONFLICT (empresa_id, produto_id) DO NOTHING
            """, nativeQuery = true)
    void criarSeNaoExistir(Long empresaId, Long produtoId);
}
