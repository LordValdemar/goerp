package br.com.goerp.usuario.aplicacao;

import br.com.goerp.compartilhado.aplicacao.CasoDeUso;
import br.com.goerp.compartilhado.dominio.Email;
import br.com.goerp.usuario.dominio.Senha;
import br.com.goerp.usuario.dominio.Usuario;
import br.com.goerp.usuario.dominio.UsuarioRepositorio;

/**
 * Primeiro acesso ao sistema: cria o administrador da plataforma se ainda não houver nenhum.
 * Pode ser executado a cada inicialização: depois do primeiro, não faz nada.
 */
@CasoDeUso
public class CriarAdministradorDaPlataforma {

    private final UsuarioRepositorio usuarios;
    private final CodificadorDeSenha codificadorDeSenha;

    public CriarAdministradorDaPlataforma(UsuarioRepositorio usuarios, CodificadorDeSenha codificadorDeSenha) {
        this.usuarios = usuarios;
        this.codificadorDeSenha = codificadorDeSenha;
    }

    /** @return verdadeiro se criou; falso se já existia um administrador da plataforma */
    public boolean executar(String nome, String email, String senha) {
        if (usuarios.existeAdministradorDaPlataforma()) {
            return false;
        }
        Email emailDoAdministrador = Email.de(email);
        if (usuarios.existeComEmail(emailDoAdministrador)) {
            throw new IllegalStateException("O e-mail " + emailDoAdministrador
                    + " já pertence a um usuário comum; use outro para o administrador da plataforma");
        }
        usuarios.salvar(Usuario.novoAdministradorDaPlataforma(nome, emailDoAdministrador,
                codificadorDeSenha.criptografar(new Senha(senha))));
        return true;
    }
}
