package br.com.goerp.compartilhado.dominio.excecao;

/** O usuário está autenticado, mas não pode fazer esta operação sobre este registro (403). */
public class OperacaoNaoPermitidaException extends RuntimeException {

    public OperacaoNaoPermitidaException(String mensagem) {
        super(mensagem);
    }
}
