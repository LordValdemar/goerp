package br.com.goerp.cadastros.produto.dominio;

/**
 * @param texto       parte da descrição ou do código interno, ou começo do código de barras (opcional)
 * @param categoriaId opcional
 * @param ativo       nulo traz todos
 */
public record FiltroDeProdutos(String texto, Long categoriaId, Boolean ativo) {

    public FiltroDeProdutos {
        texto = texto == null || texto.isBlank() ? null : texto.strip();
    }
}
