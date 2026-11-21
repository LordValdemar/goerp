package br.com.goerp.compartilhado.dominio.excecao;

/**
 * O registro foi alterado por outra pessoa depois que você o abriu. Recarregue e refaça a alteração,
 * para não sobrescrever o trabalho do outro sem perceber.
 */
public class ConflitoDeEdicaoException extends RuntimeException {

    public ConflitoDeEdicaoException(String recurso, Object id) {
        super(recurso + " " + id + " foi alterado(a) por outra pessoa; recarregue e tente de novo");
    }
}
