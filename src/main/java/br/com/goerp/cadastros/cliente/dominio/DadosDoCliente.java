package br.com.goerp.cadastros.cliente.dominio;

import static br.com.goerp.compartilhado.dominio.Textos.obrigatorio;
import static br.com.goerp.compartilhado.dominio.Textos.opcional;

import java.util.Objects;

import br.com.goerp.compartilhado.dominio.Dinheiro;
import br.com.goerp.compartilhado.dominio.Email;
import br.com.goerp.compartilhado.dominio.Endereco;
import br.com.goerp.compartilhado.dominio.Telefone;

/**
 * Dados editáveis de um cliente (tudo menos o documento, que o identifica).
 *
 * <p>Regras da inscrição estadual (NF-e): contribuinte precisa informá-la; isento e não
 * contribuinte não a informam.
 */
public record DadosDoCliente(
        String nome,
        String nomeFantasia,
        String inscricaoEstadual,
        IndicadorDeIe indicadorDeIe,
        Email email,
        Telefone telefone,
        Endereco endereco,
        Dinheiro limiteDeCredito,
        String observacoes) {

    public DadosDoCliente {
        nome = obrigatorio(nome, "nome");
        nomeFantasia = opcional(nomeFantasia);
        inscricaoEstadual = opcional(inscricaoEstadual);
        indicadorDeIe = Objects.requireNonNullElse(indicadorDeIe, IndicadorDeIe.NAO_CONTRIBUINTE);
        observacoes = opcional(observacoes);
        if (limiteDeCredito != null) {
            limiteDeCredito.exigirNaoNegativo("limiteDeCredito");
        }
        validarInscricaoEstadual(indicadorDeIe, inscricaoEstadual);
    }

    private static void validarInscricaoEstadual(IndicadorDeIe indicador, String inscricao) {
        if (indicador == IndicadorDeIe.CONTRIBUINTE && inscricao == null) {
            throw new InscricaoEstadualException("Cliente contribuinte do ICMS precisa da inscrição estadual");
        }
        if (indicador != IndicadorDeIe.CONTRIBUINTE && inscricao != null) {
            throw new InscricaoEstadualException("Inscrição estadual só pode ser informada para contribuinte do ICMS");
        }
    }
}
