package br.com.goerp.cadastros.fornecedor.web;

import br.com.goerp.cadastros.fornecedor.dominio.Fornecedor;
import br.com.goerp.compartilhado.dominio.TipoDePessoa;
import br.com.goerp.compartilhado.web.EnderecoRequest;

public record FornecedorResponse(
        Long id,
        TipoDePessoa tipoDePessoa,
        String documento,
        String nome,
        String nomeFantasia,
        String inscricaoEstadual,
        String email,
        String telefone,
        String pessoaDeContato,
        EnderecoRequest endereco,
        String observacoes,
        boolean ativo,
        Long versao) {

    static FornecedorResponse de(Fornecedor fornecedor) {
        var dados = fornecedor.dados();
        return new FornecedorResponse(fornecedor.id(), fornecedor.tipoDePessoa(), fornecedor.documento().formatado(),
                dados.nome(), dados.nomeFantasia(), dados.inscricaoEstadual(),
                dados.email() == null ? null : dados.email().valor(),
                dados.telefone() == null ? null : dados.telefone().formatado(),
                dados.pessoaDeContato(), EnderecoRequest.de(dados.endereco()), dados.observacoes(),
                fornecedor.ativo(), fornecedor.versao());
    }
}
