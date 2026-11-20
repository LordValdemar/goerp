package br.com.goerp.compartilhado.dominio;

import br.com.goerp.compartilhado.dominio.excecao.CampoObrigatorioException;
import br.com.goerp.compartilhado.dominio.excecao.DocumentoInvalidoException;

/** CPF ou CNPJ. O tipo de pessoa (física ou jurídica) é deduzido do documento. */
public sealed interface Documento permits Cpf, Cnpj {

    int DIGITOS_DO_CPF = 11;
    int DIGITOS_DO_CNPJ = 14;

    String valor();

    String formatado();

    TipoDePessoa tipoDePessoa();

    static Documento de(String texto) {
        if (texto == null || texto.isBlank()) {
            throw new CampoObrigatorioException("documento");
        }
        return switch (Digitos.somente(texto).length()) {
            case DIGITOS_DO_CPF -> Cpf.de(texto);
            case DIGITOS_DO_CNPJ -> Cnpj.de(texto);
            default -> throw new DocumentoInvalidoException(texto);
        };
    }
}
