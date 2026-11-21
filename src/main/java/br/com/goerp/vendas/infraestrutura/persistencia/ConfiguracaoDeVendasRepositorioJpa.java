package br.com.goerp.vendas.infraestrutura.persistencia;

import java.math.BigDecimal;

import org.springframework.stereotype.Repository;

import br.com.goerp.vendas.dominio.ConfiguracaoDeVendasRepositorio;
import br.com.goerp.vendas.dominio.PoliticaDeDesconto;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
class ConfiguracaoDeVendasRepositorioJpa implements ConfiguracaoDeVendasRepositorio {

    private final ConfiguracaoDeVendasJpaRepository jpa;

    @Override
    public BigDecimal descontoMaximoPercentual() {
        return jpa.findFirstBy().map(ConfiguracaoDeVendasJpa::getDescontoMaximoPercentual)
                .orElse(PoliticaDeDesconto.PERCENTUAL_PADRAO);
    }

    @Override
    public void definirDescontoMaximoPercentual(Long empresaId, BigDecimal percentual) {
        ConfiguracaoDeVendasJpa registro = jpa.findFirstBy().orElseGet(() -> {
            var nova = new ConfiguracaoDeVendasJpa();
            nova.setEmpresaId(empresaId);
            return nova;
        });
        registro.setDescontoMaximoPercentual(percentual);
        jpa.save(registro);
    }
}
