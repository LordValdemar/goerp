package br.com.goerp.compartilhado.aplicacao;

/** Porta: a empresa em que o usuário autenticado está trabalhando. */
public interface EmpresaAtual {

    /** @throws IllegalStateException se não houver usuário autenticado em uma empresa */
    Long id();
}
