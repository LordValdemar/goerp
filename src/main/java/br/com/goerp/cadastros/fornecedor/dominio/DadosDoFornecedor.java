package br.com.goerp.cadastros.fornecedor.dominio;

import static br.com.goerp.compartilhado.dominio.Textos.obrigatorio;
import static br.com.goerp.compartilhado.dominio.Textos.opcional;

import br.com.goerp.compartilhado.dominio.Email;
import br.com.goerp.compartilhado.dominio.Endereco;
import br.com.goerp.compartilhado.dominio.Telefone;

/**
 * Dados editáveis de um fornecedor (tudo menos o documento).
 *
 * @param pessoaDeContato com quem falar no fornecedor (vendedor, representante)
 */
public record DadosDoFornecedor(
        String nome,
        String nomeFantasia,
        String inscricaoEstadual,
        Email email,
        Telefone telefone,
        String pessoaDeContato,
        Endereco endereco,
        String observacoes) {

    public DadosDoFornecedor {
        nome = obrigatorio(nome, "nome");
        nomeFantasia = opcional(nomeFantasia);
        inscricaoEstadual = opcional(inscricaoEstadual);
        pessoaDeContato = opcional(pessoaDeContato);
        observacoes = opcional(observacoes);
    }
}
