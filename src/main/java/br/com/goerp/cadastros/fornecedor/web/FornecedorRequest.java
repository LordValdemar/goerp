package br.com.goerp.cadastros.fornecedor.web;

import br.com.goerp.cadastros.fornecedor.dominio.DadosDoFornecedor;
import br.com.goerp.compartilhado.dominio.Email;
import br.com.goerp.compartilhado.dominio.Telefone;
import br.com.goerp.compartilhado.web.EnderecoRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * @param documento só no cadastro (depois não muda)
 * @param versao    só na alteração: a versão que foi lida, para detectar edição simultânea
 */
public record FornecedorRequest(
        String documento,
        @NotBlank @Size(max = 150) String nome,
        @Size(max = 150) String nomeFantasia,
        @Size(max = 20) String inscricaoEstadual,
        String email,
        String telefone,
        @Size(max = 100) String pessoaDeContato,
        @Valid EnderecoRequest endereco,
        @Size(max = 1000) String observacoes,
        Long versao) {

    DadosDoFornecedor paraDados() {
        return new DadosDoFornecedor(nome, nomeFantasia, inscricaoEstadual,
                email == null || email.isBlank() ? null : Email.de(email),
                telefone == null || telefone.isBlank() ? null : Telefone.de(telefone),
                pessoaDeContato,
                endereco == null ? null : endereco.paraDominio(),
                observacoes);
    }
}
