package br.com.goerp.vendas.infraestrutura.persistencia;

import java.math.BigDecimal;
import java.util.EnumMap;
import java.util.Map;

import org.springframework.stereotype.Repository;

import br.com.goerp.compartilhado.dominio.Dinheiro;
import br.com.goerp.financeiro.dominio.FormaDePagamento;
import br.com.goerp.vendas.dominio.VendasDoCaixa;
import lombok.RequiredArgsConstructor;

/** Soma os pagamentos das vendas finalizadas do caixa; do dinheiro, tira o troco devolvido. */
@Repository
@RequiredArgsConstructor
class VendasDoCaixaJpa implements VendasDoCaixa {

    private final VendaJpaRepository jpa;

    @Override
    public Map<FormaDePagamento, Dinheiro> recebidoPorForma(Long caixaId) {
        Map<FormaDePagamento, Dinheiro> recebido = new EnumMap<>(FormaDePagamento.class);
        jpa.pagamentosPorForma(caixaId).forEach(linha ->
                recebido.put((FormaDePagamento) linha[0], Dinheiro.de((BigDecimal) linha[1])));
        Dinheiro troco = Dinheiro.de(jpa.trocoDado(caixaId));
        recebido.computeIfPresent(FormaDePagamento.DINHEIRO, (forma, valor) -> valor.subtrair(troco));
        return recebido;
    }
}
