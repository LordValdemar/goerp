package br.com.goerp.compartilhado.dominio.excecao;

public class DocumentoInvalidoException extends ExcecaoDeDominio {

    public DocumentoInvalidoException(String valor) {
        super("Documento inválido (informe CPF com 11 dígitos ou CNPJ com 14): " + valor);
    }
}
