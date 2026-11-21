package br.com.goerp.compartilhado.infraestrutura.persistencia;

import org.springframework.stereotype.Component;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;

/**
 * Números sequenciais por empresa (vendas, pedidos...). Um único comando atômico cria o contador
 * ou soma 1 e devolve o número; a linha fica travada até o fim da transação, então não há
 * repetição, e se a transação for desfeita o número volta (sem buracos).
 *
 * <p>SQL nativo não passa pelo filtro de empresa do Hibernate: a empresa vem explicitamente do token.
 */
@Component
@RequiredArgsConstructor
public class ContadorSequencial {

    /** Tabelas de contador conhecidas (o nome nunca vem de entrada do usuário). */
    public enum Serie {
        VENDA("numeracao_venda"),
        PEDIDO_DE_VENDA("numeracao_pedido_venda");

        private final String tabela;

        Serie(String tabela) {
            this.tabela = tabela;
        }
    }

    private final EntityManager entityManager;

    public long proximo(Serie serie, Long empresaId) {
        Number numero = (Number) entityManager.createNativeQuery("""
                INSERT INTO %1$s (empresa_id, ultimo_numero) VALUES (:empresaId, 1)
                ON CONFLICT (empresa_id) DO UPDATE SET ultimo_numero = %1$s.ultimo_numero + 1
                RETURNING ultimo_numero""".formatted(serie.tabela))
                .setParameter("empresaId", empresaId)
                .getSingleResult();
        return numero.longValue();
    }
}
