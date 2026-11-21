package br.com.goerp.compartilhado.dominio.excecao;

/** Base das exceções que representam uma regra de negócio violada. */
public abstract class ExcecaoDeDominio extends RuntimeException {

    protected ExcecaoDeDominio(String mensagem) {
        super(mensagem);
    }
}
