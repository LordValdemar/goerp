package br.com.goerp.cadastros.fornecedor.dominio;

/**
 * @param texto parte do nome, do nome fantasia ou do documento (opcional)
 * @param ativo filtra por situação; nulo traz todos
 */
public record FiltroDeFornecedores(String texto, Boolean ativo) {

    public FiltroDeFornecedores {
        texto = texto == null || texto.isBlank() ? null : texto.strip();
    }
}
