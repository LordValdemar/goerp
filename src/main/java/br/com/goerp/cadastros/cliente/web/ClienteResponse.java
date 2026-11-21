package br.com.goerp.cadastros.cliente.web;

import java.math.BigDecimal;

import br.com.goerp.cadastros.cliente.dominio.Cliente;
import br.com.goerp.cadastros.cliente.dominio.IndicadorDeIe;
import br.com.goerp.compartilhado.dominio.TipoDePessoa;
import br.com.goerp.compartilhado.web.EnderecoRequest;

public record ClienteResponse(
        Long id,
        TipoDePessoa tipoDePessoa,
        String documento,
        String nome,
        String nomeFantasia,
        String inscricaoEstadual,
        IndicadorDeIe indicadorDeIe,
        String email,
        String telefone,
        EnderecoRequest endereco,
        BigDecimal limiteDeCredito,
        String observacoes,
        boolean ativo,
        Long versao) {

    static ClienteResponse de(Cliente cliente) {
        var dados = cliente.dados();
        return new ClienteResponse(cliente.id(), cliente.tipoDePessoa(), cliente.documento().formatado(), dados.nome(),
                dados.nomeFantasia(), dados.inscricaoEstadual(), dados.indicadorDeIe(),
                dados.email() == null ? null : dados.email().valor(),
                dados.telefone() == null ? null : dados.telefone().formatado(),
                EnderecoRequest.de(dados.endereco()),
                dados.limiteDeCredito() == null ? null : dados.limiteDeCredito().valor(),
                dados.observacoes(), cliente.ativo(), cliente.versao());
    }
}
