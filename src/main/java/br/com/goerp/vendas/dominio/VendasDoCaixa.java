package br.com.goerp.vendas.dominio;

import java.util.Map;

import br.com.goerp.compartilhado.dominio.Dinheiro;

/** Quanto as vendas (não canceladas) de um caixa trouxeram, por forma de pagamento. */
public interface VendasDoCaixa {

    Map<FormaDePagamento, Dinheiro> recebidoPorForma(Long caixaId);
}
