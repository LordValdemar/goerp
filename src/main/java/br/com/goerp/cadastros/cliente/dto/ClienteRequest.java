package br.com.goerp.cadastros.cliente.dto;

import br.com.goerp.cadastros.cliente.TipoPessoa;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ClienteRequest(
        @NotNull TipoPessoa tipoPessoa,
        @NotBlank @Size(max = 150) String nome,
        @NotBlank @Pattern(regexp = "\\d{11}|\\d{14}", message = "deve conter 11 (CPF) ou 14 (CNPJ) dígitos")
        String cpfCnpj,
        @Email @Size(max = 150) String email,
        @Size(max = 20) String telefone) {
}
