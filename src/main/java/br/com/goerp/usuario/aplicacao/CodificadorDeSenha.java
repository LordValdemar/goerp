package br.com.goerp.usuario.aplicacao;

import br.com.goerp.usuario.dominio.Senha;
import br.com.goerp.usuario.dominio.SenhaCriptografada;

/** Porta para o algoritmo de hash de senhas (implementado com BCrypt na infraestrutura). */
public interface CodificadorDeSenha {

    SenhaCriptografada criptografar(Senha senha);

    /** Recebe o texto digitado (sem aplicar a política), pois senhas antigas podem não atendê-la. */
    boolean confere(String senhaDigitada, SenhaCriptografada senhaCriptografada);
}
