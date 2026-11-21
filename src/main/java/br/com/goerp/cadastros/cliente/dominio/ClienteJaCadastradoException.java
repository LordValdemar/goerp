package br.com.goerp.cadastros.cliente.dominio;

import br.com.goerp.compartilhado.dominio.Documento;
import br.com.goerp.compartilhado.dominio.excecao.ExcecaoDeDominio;

public class ClienteJaCadastradoException extends ExcecaoDeDominio {

    public ClienteJaCadastradoException(Documento documento) {
        super("Já existe um cliente com o documento " + documento.formatado());
    }
}
