package br.com.goerp.vendas.infraestrutura.persistencia;

import org.springframework.stereotype.Repository;

import br.com.goerp.vendas.dominio.NumeracaoDeVendas;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;

/**
 * Um único comando atômico: cria o contador da empresa ou soma 1, e devolve o número. A linha
 * fica travada até o fim da transação, então duas vendas nunca recebem o mesmo número; se a
 * venda falhar, o incremento é desfeito junto.
 *
 * <p>SQL nativo não passa pelo filtro de empresa do Hibernate: a empresa vem explicitamente do token.
 */
@Repository
@RequiredArgsConstructor
class NumeracaoDeVendasJpa implements NumeracaoDeVendas {

    private final EntityManager entityManager;

    @Override
    public long proximo(Long empresaId) {
        Number numero = (Number) entityManager.createNativeQuery("""
                INSERT INTO numeracao_venda (empresa_id, ultimo_numero) VALUES (:empresaId, 1)
                ON CONFLICT (empresa_id) DO UPDATE SET ultimo_numero = numeracao_venda.ultimo_numero + 1
                RETURNING ultimo_numero""")
                .setParameter("empresaId", empresaId)
                .getSingleResult();
        return numero.longValue();
    }
}
