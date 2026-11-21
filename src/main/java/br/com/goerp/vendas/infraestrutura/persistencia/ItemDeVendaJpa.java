package br.com.goerp.vendas.infraestrutura.persistencia;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Embeddable
@NoArgsConstructor
@AllArgsConstructor
class ItemDeVendaJpa {

    @Column(name = "produto_id", nullable = false)
    private Long produtoId;

    @Column(nullable = false)
    private String codigo;

    @Column(nullable = false)
    private String descricao;

    @Column(nullable = false)
    private BigDecimal quantidade;

    @Column(name = "preco_unitario", nullable = false)
    private BigDecimal precoUnitario;

    @Column(nullable = false)
    private BigDecimal desconto;
}
