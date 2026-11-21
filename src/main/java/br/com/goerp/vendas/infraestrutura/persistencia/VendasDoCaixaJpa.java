package br.com.goerp.vendas.infraestrutura.persistencia;

import java.util.Map;

import org.springframework.stereotype.Repository;

import br.com.goerp.compartilhado.dominio.Dinheiro;
import br.com.goerp.vendas.dominio.FormaDePagamento;
import br.com.goerp.vendas.dominio.VendasDoCaixa;

/**
 * Ainda não há vendas gravadas (chegam no próximo passo do PDV): todo caixa recebeu zero.
 * Será trocado pela soma dos pagamentos das vendas do caixa.
 */
@Repository
class VendasDoCaixaJpa implements VendasDoCaixa {

    @Override
    public Map<FormaDePagamento, Dinheiro> recebidoPorForma(Long caixaId) {
        return Map.of();
    }
}
