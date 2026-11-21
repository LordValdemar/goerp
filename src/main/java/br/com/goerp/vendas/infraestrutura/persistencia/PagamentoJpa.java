package br.com.goerp.vendas.infraestrutura.persistencia;

import java.math.BigDecimal;

import br.com.goerp.financeiro.dominio.FormaDePagamento;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Embeddable
@NoArgsConstructor
@AllArgsConstructor
class PagamentoJpa {

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FormaDePagamento forma;

    @Column(nullable = false)
    private BigDecimal valor;
}
