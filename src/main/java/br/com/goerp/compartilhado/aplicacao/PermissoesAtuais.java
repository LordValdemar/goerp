package br.com.goerp.compartilhado.aplicacao;

/**
 * Porta: as permissões do usuário autenticado. Para regras que dependem de quem está operando
 * (ex.: fechar o caixa de outro operador só com permissão de gerente), além do {@code @PreAuthorize}.
 */
public interface PermissoesAtuais {

    boolean possui(String permissao);
}
