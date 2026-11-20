package br.com.goerp.cadastros.fornecedor.dominio;

import br.com.goerp.compartilhado.dominio.Documento;
import br.com.goerp.compartilhado.dominio.excecao.ExcecaoDeDominio;

public class FornecedorJaCadastradoException extends ExcecaoDeDominio {

    public FornecedorJaCadastradoException(Documento documento) {
        super("Já existe um fornecedor com o documento " + documento.formatado());
    }
}
