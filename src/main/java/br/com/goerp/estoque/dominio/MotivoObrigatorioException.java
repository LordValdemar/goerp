package br.com.goerp.estoque.dominio;

import br.com.goerp.compartilhado.dominio.excecao.ExcecaoDeDominio;

public class MotivoObrigatorioException extends ExcecaoDeDominio {

    public MotivoObrigatorioException() {
        super("Ajustes e estornos precisam de um motivo");
    }
}
