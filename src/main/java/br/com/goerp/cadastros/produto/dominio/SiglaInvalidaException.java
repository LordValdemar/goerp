package br.com.goerp.cadastros.produto.dominio;

import br.com.goerp.compartilhado.dominio.excecao.ExcecaoDeDominio;

public class SiglaInvalidaException extends ExcecaoDeDominio {

    public SiglaInvalidaException(String sigla) {
        super("Sigla de unidade inválida (1 a " + UnidadeDeMedida.TAMANHO_MAXIMO_DA_SIGLA + " letras ou números): " + sigla);
    }
}
