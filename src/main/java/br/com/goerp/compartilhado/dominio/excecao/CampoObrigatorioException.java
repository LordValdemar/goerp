package br.com.goerp.compartilhado.dominio.excecao;

public class CampoObrigatorioException extends ExcecaoDeDominio {

    public CampoObrigatorioException(String campo) {
        super("O campo '" + campo + "' é obrigatório");
    }
}
