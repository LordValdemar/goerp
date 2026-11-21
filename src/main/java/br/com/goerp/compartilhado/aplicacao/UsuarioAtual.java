package br.com.goerp.compartilhado.aplicacao;

/** Porta: o usuário autenticado na requisição atual. */
public interface UsuarioAtual {

    /** @throws IllegalStateException se não houver usuário autenticado */
    Long id();
}
