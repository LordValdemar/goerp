package br.com.goerp.estoque.infraestrutura.persistencia;

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
class ContagemJpa {

    @Column(name = "produto_id", nullable = false)
    private Long produtoId;

    @Column(name = "quantidade_contada", nullable = false)
    private BigDecimal quantidadeContada;
}
