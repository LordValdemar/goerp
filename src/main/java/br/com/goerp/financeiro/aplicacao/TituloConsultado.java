package br.com.goerp.financeiro.aplicacao;

import br.com.goerp.financeiro.dominio.Titulo;

/** O título com a situação de prazo no dia de hoje (no fuso da empresa). */
public record TituloConsultado(Titulo titulo, boolean vencido, long diasEmAtraso) {
}
