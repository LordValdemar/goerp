package br.com.goerp.cadastros.cliente.web;

import java.math.BigDecimal;

import br.com.goerp.cadastros.cliente.dominio.DadosDoCliente;
import br.com.goerp.cadastros.cliente.dominio.IndicadorDeIe;
import br.com.goerp.compartilhado.dominio.Dinheiro;
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
public record ClienteRequest(
        String documento,
        @NotBlank @Size(max = 150) String nome,
        @Size(max = 150) String nomeFantasia,
        @Size(max = 20) String inscricaoEstadual,
        IndicadorDeIe indicadorDeIe,
        String email,
        String telefone,
        @Valid EnderecoRequest endereco,
        BigDecimal limiteDeCredito,
        @Size(max = 1000) String observacoes,
        Long versao) {

    DadosDoCliente paraDados() {
        return new DadosDoCliente(nome, nomeFantasia, inscricaoEstadual, indicadorDeIe,
                email == null || email.isBlank() ? null : Email.de(email),
                telefone == null || telefone.isBlank() ? null : Telefone.de(telefone),
                endereco == null ? null : endereco.paraDominio(),
                limiteDeCredito == null ? null : Dinheiro.de(limiteDeCredito),
                observacoes);
    }
}
