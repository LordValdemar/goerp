package br.com.goerp.compartilhado.infraestrutura.persistencia;

import br.com.goerp.compartilhado.dominio.Cep;
import br.com.goerp.compartilhado.dominio.Endereco;
import br.com.goerp.compartilhado.dominio.Uf;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** Endereço gravado em colunas da própria tabela (cep, logradouro, ..., uf). */
@Getter
@Embeddable
@NoArgsConstructor
public class EnderecoJpa {

    @Column(name = "cep")
    private Cep cep;

    @Column(name = "logradouro")
    private String logradouro;

    @Column(name = "numero")
    private String numero;

    @Column(name = "complemento")
    private String complemento;

    @Column(name = "bairro")
    private String bairro;

    @Column(name = "municipio")
    private String municipio;

    @Column(name = "codigo_ibge_municipio")
    private String codigoIbgeDoMunicipio;

    @Enumerated(EnumType.STRING)
    @Column(name = "uf")
    private Uf uf;

    public static EnderecoJpa de(Endereco endereco) {
        if (endereco == null) {
            return null;
        }
        var jpa = new EnderecoJpa();
        jpa.cep = endereco.cep();
        jpa.logradouro = endereco.logradouro();
        jpa.numero = endereco.numero();
        jpa.complemento = endereco.complemento();
        jpa.bairro = endereco.bairro();
        jpa.municipio = endereco.municipio();
        jpa.codigoIbgeDoMunicipio = endereco.codigoIbgeDoMunicipio();
        jpa.uf = endereco.uf();
        return jpa;
    }

    /** O Hibernate devolve o embutido vazio (ou nulo) quando todas as colunas são nulas. */
    public static Endereco paraDominio(EnderecoJpa jpa) {
        if (jpa == null || jpa.cep == null) {
            return null;
        }
        return new Endereco(jpa.cep, jpa.logradouro, jpa.numero, jpa.complemento, jpa.bairro, jpa.municipio,
                jpa.codigoIbgeDoMunicipio, jpa.uf);
    }
}
