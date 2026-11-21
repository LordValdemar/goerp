package br.com.goerp.vendas.aplicacao;

import java.math.BigDecimal;

import br.com.goerp.compartilhado.aplicacao.CasoDeUso;
import br.com.goerp.compartilhado.aplicacao.EmpresaAtual;
import br.com.goerp.vendas.dominio.ConfiguracaoDeVendasRepositorio;
import br.com.goerp.vendas.dominio.PoliticaDeDesconto;

@CasoDeUso
public class ConfigurarVendas {

    private final ConfiguracaoDeVendasRepositorio configuracao;
    private final EmpresaAtual empresaAtual;

    public ConfigurarVendas(ConfiguracaoDeVendasRepositorio configuracao, EmpresaAtual empresaAtual) {
        this.configuracao = configuracao;
        this.empresaAtual = empresaAtual;
    }

    public BigDecimal descontoMaximo() {
        return configuracao.descontoMaximoPercentual();
    }

    public BigDecimal definirDescontoMaximo(BigDecimal percentual) {
        new PoliticaDeDesconto(percentual, false);
        configuracao.definirDescontoMaximoPercentual(empresaAtual.id(), percentual);
        return percentual;
    }
}
