package br.com.goerp.usuario.infraestrutura.seguranca;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import br.com.goerp.usuario.aplicacao.CodificadorDeSenha;
import br.com.goerp.usuario.dominio.Senha;
import br.com.goerp.usuario.dominio.SenhaCriptografada;

@Component
public class CodificadorDeSenhaBCrypt implements CodificadorDeSenha {

    private static final int CUSTO = 12;

    private final BCryptPasswordEncoder bcrypt = new BCryptPasswordEncoder(CUSTO);

    @Override
    public SenhaCriptografada criptografar(Senha senha) {
        return new SenhaCriptografada(bcrypt.encode(senha.valor()));
    }

    @Override
    public boolean confere(String senhaDigitada, SenhaCriptografada senhaCriptografada) {
        return senhaDigitada != null && bcrypt.matches(senhaDigitada, senhaCriptografada.hash());
    }
}
