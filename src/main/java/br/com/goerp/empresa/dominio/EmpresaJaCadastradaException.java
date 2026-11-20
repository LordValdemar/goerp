package br.com.goerp.empresa.dominio;

import br.com.goerp.compartilhado.dominio.Cnpj;
import br.com.goerp.compartilhado.dominio.excecao.ExcecaoDeDominio;

public class EmpresaJaCadastradaException extends ExcecaoDeDominio {

    public EmpresaJaCadastradaException(Cnpj cnpj) {
        super("Já existe uma empresa com o CNPJ " + cnpj.formatado());
    }
}
