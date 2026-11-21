package br.com.goerp.compartilhado.dominio;

import static br.com.goerp.compartilhado.dominio.Textos.obrigatorio;
import static br.com.goerp.compartilhado.dominio.Textos.opcional;

import java.util.Objects;

import br.com.goerp.compartilhado.dominio.excecao.CodigoIbgeInvalidoException;

/**
 * Endereço completo, no formato exigido pela NF-e: inclui o código IBGE do município,
 * que precisa começar pelo código da UF.
 */
public record Endereco(
        Cep cep,
        String logradouro,
        String numero,
        String complemento,
        String bairro,
        String municipio,
        String codigoIbgeDoMunicipio,
        Uf uf) {

    private static final int DIGITOS_DO_CODIGO_IBGE = 7;
    private static final String SEM_NUMERO = "S/N";

    public Endereco {
        Objects.requireNonNull(cep, "cep");
        Objects.requireNonNull(uf, "uf");
        logradouro = obrigatorio(logradouro, "logradouro");
        numero = numero == null || numero.isBlank() ? SEM_NUMERO : numero.strip();
        complemento = opcional(complemento);
        bairro = obrigatorio(bairro, "bairro");
        municipio = obrigatorio(municipio, "municipio");
        codigoIbgeDoMunicipio = validarCodigoIbge(codigoIbgeDoMunicipio, uf);
    }

    private static String validarCodigoIbge(String codigo, Uf uf) {
        String digitos = Digitos.somente(obrigatorio(codigo, "codigoIbgeDoMunicipio"));
        boolean formatoValido = digitos.length() == DIGITOS_DO_CODIGO_IBGE;
        if (!formatoValido || !digitos.startsWith(String.valueOf(uf.codigoIbge()))) {
            throw new CodigoIbgeInvalidoException(codigo + " (UF " + uf + ")");
        }
        return digitos;
    }
}
