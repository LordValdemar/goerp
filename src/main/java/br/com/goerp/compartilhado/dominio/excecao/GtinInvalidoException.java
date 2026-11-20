package br.com.goerp.compartilhado.dominio.excecao;

public class GtinInvalidoException extends ExcecaoDeDominio {

    public GtinInvalidoException(String valor) {
        super("Código de barras (GTIN/EAN) inválido: " + valor);
    }
}
