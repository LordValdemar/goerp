package br.com.goerp.vendas.infraestrutura.persistencia;

import java.math.BigDecimal;
import java.time.Instant;

import br.com.goerp.vendas.dominio.TipoDeMovimentoDeCaixa;
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
class MovimentoDeCaixaJpa {

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoDeMovimentoDeCaixa tipo;

    @Column(nullable = false)
    private BigDecimal valor;

    private String motivo;

    @Column(name = "usuario_id")
    private Long usuarioId;

    @Column(nullable = false)
    private Instant momento;
}
