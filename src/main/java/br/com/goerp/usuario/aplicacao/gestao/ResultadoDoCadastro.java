package br.com.goerp.usuario.aplicacao.gestao;

/** @param usuarioJaExistia verdadeiro quando só foi dado acesso a alguém já cadastrado no sistema */
public record ResultadoDoCadastro(UsuarioDaEmpresa usuarioDaEmpresa, boolean usuarioJaExistia) {
}
