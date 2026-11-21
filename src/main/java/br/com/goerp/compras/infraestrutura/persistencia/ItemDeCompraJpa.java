package br.com.goerp.compras.infraestrutura.persistencia;

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
class ItemDeCompraJpa {

    @Column(name = "produto_id", nullable = false)
    private Long produtoId;

    @Column(nullable = false)
    private String codigo;

    @Column(nullable = false)
    private String descricao;

    @Column(nullable = false)
    private BigDecimal quantidade;

    @Column(name = "custo_negociado", nullable = false)
    private BigDecimal custoNegociado;

    @Column(nullable = false)
    private BigDecimal recebida;
}
