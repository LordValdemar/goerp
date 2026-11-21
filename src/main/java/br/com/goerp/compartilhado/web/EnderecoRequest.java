package br.com.goerp.compartilhado.web;

import br.com.goerp.compartilhado.dominio.Cep;
import br.com.goerp.compartilhado.dominio.Endereco;
import br.com.goerp.compartilhado.dominio.Uf;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record EnderecoRequest(
        @NotBlank String cep,
        @NotBlank @Size(max = 150) String logradouro,
        @Size(max = 20) String numero,
        @Size(max = 100) String complemento,
        @NotBlank @Size(max = 100) String bairro,
        @NotBlank @Size(max = 100) String municipio,
        @NotBlank String codigoIbgeDoMunicipio,
        @NotBlank String uf) {

    public Endereco paraDominio() {
        return new Endereco(Cep.de(cep), logradouro, numero, complemento, bairro, municipio, codigoIbgeDoMunicipio,
                Uf.de(uf));
    }

    public static EnderecoRequest de(Endereco endereco) {
        if (endereco == null) {
            return null;
        }
        return new EnderecoRequest(endereco.cep().formatado(), endereco.logradouro(), endereco.numero(),
                endereco.complemento(), endereco.bairro(), endereco.municipio(), endereco.codigoIbgeDoMunicipio(),
                endereco.uf().name());
    }
}
