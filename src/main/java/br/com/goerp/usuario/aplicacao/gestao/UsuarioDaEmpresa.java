package br.com.goerp.usuario.aplicacao.gestao;

import br.com.goerp.usuario.dominio.Perfil;
import br.com.goerp.usuario.dominio.Usuario;
import br.com.goerp.usuario.dominio.VinculoEmpresa;

/** Um usuário visto de dentro de uma empresa: seus dados, o acesso (vínculo) e o perfil nela. */
public record UsuarioDaEmpresa(Usuario usuario, VinculoEmpresa vinculo, Perfil perfil) {
}
