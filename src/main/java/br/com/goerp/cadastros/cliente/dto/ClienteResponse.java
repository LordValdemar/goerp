package br.com.goerp.cadastros.cliente.dto;

import java.time.OffsetDateTime;

import br.com.goerp.cadastros.cliente.Cliente;
import br.com.goerp.cadastros.cliente.TipoPessoa;

public record ClienteResponse(
        Long id,
        TipoPessoa tipoPessoa,
        String nome,
        String cpfCnpj,
        String email,
        String telefone,
        boolean ativo,
        OffsetDateTime criadoEm,
        OffsetDateTime atualizadoEm) {

    public static ClienteResponse de(Cliente c) {
        return new ClienteResponse(c.getId(), c.getTipoPessoa(), c.getNome(), c.getCpfCnpj(),
                c.getEmail(), c.getTelefone(), c.isAtivo(), c.getCriadoEm(), c.getAtualizadoEm());
    }
}
