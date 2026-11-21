package br.com.goerp.estoque.aplicacao;

import br.com.goerp.compartilhado.aplicacao.CasoDeUso;
import br.com.goerp.estoque.dominio.ConfiguracaoDeEstoqueRepositorio;

@CasoDeUso
public class ConsultarConfiguracaoDeEstoque {

    private final ConfiguracaoDeEstoqueRepositorio configuracao;

    public ConsultarConfiguracaoDeEstoque(ConfiguracaoDeEstoqueRepositorio configuracao) {
        this.configuracao = configuracao;
    }

    public boolean permiteEstoqueNegativo() {
        return configuracao.permiteEstoqueNegativo();
    }
}
