package br.com.goerp.cadastros.cliente.dominio;

/**
 * @param texto parte do nome, do nome fantasia ou do documento (opcional)
 * @param ativo filtra por situação; nulo traz todos
 */
public record FiltroDeClientes(String texto, Boolean ativo) {

    public FiltroDeClientes {
        texto = texto == null || texto.isBlank() ? null : texto.strip();
    }
}
