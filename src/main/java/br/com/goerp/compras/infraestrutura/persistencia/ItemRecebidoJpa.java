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
class ItemRecebidoJpa {

    @Column(name = "produto_id", nullable = false)
    private Long produtoId;

    @Column(nullable = false)
    private BigDecimal quantidade;

    @Column(name = "custo_unitario", nullable = false)
    private BigDecimal custoUnitario;
}
