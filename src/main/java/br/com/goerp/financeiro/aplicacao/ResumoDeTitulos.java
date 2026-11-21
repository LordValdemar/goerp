package br.com.goerp.financeiro.aplicacao;

import br.com.goerp.financeiro.dominio.TotalDeTitulos;

/** O que está em aberto, quanto já venceu e quanto vence hoje. */
public record ResumoDeTitulos(TotalDeTitulos emAberto, TotalDeTitulos vencidos, TotalDeTitulos vencemHoje) {
}
