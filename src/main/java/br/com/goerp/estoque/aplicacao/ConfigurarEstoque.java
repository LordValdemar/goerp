package br.com.goerp.estoque.aplicacao;

import br.com.goerp.compartilhado.aplicacao.CasoDeUso;
import br.com.goerp.compartilhado.aplicacao.EmpresaAtual;
import br.com.goerp.estoque.dominio.ConfiguracaoDeEstoqueRepositorio;

/**
 * Define se a empresa aceita saldo negativo. Recusar (padrão) protege o saldo; aceitar evita
 * travar o caixa por erro de cadastro, e o saldo é corrigido no inventário.
 */
@CasoDeUso
public class ConfigurarEstoque {

    private final ConfiguracaoDeEstoqueRepositorio configuracao;
    private final EmpresaAtual empresaAtual;

    public ConfigurarEstoque(ConfiguracaoDeEstoqueRepositorio configuracao, EmpresaAtual empresaAtual) {
        this.configuracao = configuracao;
        this.empresaAtual = empresaAtual;
    }

    public boolean executar(boolean permiteEstoqueNegativo) {
        configuracao.definirPermiteEstoqueNegativo(empresaAtual.id(), permiteEstoqueNegativo);
        return permiteEstoqueNegativo;
    }
}
